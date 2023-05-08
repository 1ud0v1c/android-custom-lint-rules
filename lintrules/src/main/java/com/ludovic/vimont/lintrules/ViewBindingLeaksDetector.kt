package com.ludovic.vimont.lintrules

import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.google.common.annotations.Beta
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UField
import org.jetbrains.uast.UastFacade

@Beta
class ViewBindingLeaksDetector : Detector(), SourceCodeScanner {
    companion object {
        private const val FRAGMENT_CLASS = "androidx.fragment.app.Fragment"
        private const val ON_DESTROY_VIEW_METHOD_NAME = "onDestroyView"
        private const val VIEW_BINDING_CLASS = "androidx.viewbinding.ViewBinding"
    }

    override fun getApplicableUastTypes() = listOf(UClass::class.java)

    override fun applicableSuperClasses() = listOf(FRAGMENT_CLASS)

    override fun visitClass(context: JavaContext, declaration: UClass) {
        super.visitClass(context, declaration)

        context.findViewBindingFields(declaration).forEach { field: UField ->
            val onDestroyMethod = declaration.findMethodsByName(ON_DESTROY_VIEW_METHOD_NAME).firstOrNull()
            if (onDestroyMethod == null) {
                val message = "Don't forget to implements $ON_DESTROY_VIEW_METHOD_NAME inside a fragment " +
                        "to avoid memory leaks while using ViewBinding."
                report(context, declaration, message)
                return
            }
            UastFacade.getMethodBody(onDestroyMethod as PsiMethod)?.let { uExpression: UExpression ->
                val regex = "${field.name}(.*)+=(.*)null".toRegex()
                if (uExpression.asSourceString().contains(regex).not()) {
                    val message = "You need to set the field: \"${field.name}\" to null to avoid memory leaks."
                    report(context, declaration, message, context.getLocation(uExpression))
                }
            }
        }
    }

    private fun JavaContext.findViewBindingFields(declaration: UClass): List<UField> {
        val fields = arrayListOf<UField>()
        declaration.fields.forEach { field: UField ->
            val type = field.type as? PsiClassType ?: return@forEach
            type.resolve()?.let { psiClass: PsiClass ->
                if (evaluator.implementsInterface(psiClass, VIEW_BINDING_CLASS)) {
                    fields.add(field)
                }
            }
        }
        return fields
    }

    private fun report(context: JavaContext,
                       declaration: UClass,
                       message: String = "Be careful about MemoryLeaks using ViewBinding inside a Fragment.",
                       location: Location = context.getNameLocation(declaration)) {
        context.report(
            issue = VIEW_BINDING_ISSUE,
            scopeClass = declaration,
            location = location,
            message = message,
        )
    }
}