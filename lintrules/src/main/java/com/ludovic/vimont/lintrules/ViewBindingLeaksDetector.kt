package com.ludovic.vimont.lintrules

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.google.common.annotations.Beta
import com.intellij.lang.jvm.JvmMethod
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UField
import org.jetbrains.uast.UastFacade

@Beta
class ViewBindingLeaksDetector : Detector(), Detector.UastScanner, SourceCodeScanner {
    companion object {
        private const val VIEW_BINDING_CLASS = "androidx.viewbinding.ViewBinding"
        private const val FRAGMENT_CLASS = "androidx.fragment.app.Fragment"
        private const val ON_DESTROY_VIEW_METHOD_NAME = "onDestroyView"

        val ISSUE: Issue = Issue.create(
            id = "ViewBindingLeaksDetector",
            briefDescription = "Be careful about MemoryLeaks using ViewBinding inside a Fragment",
            explanation = """
                You should be careful about Memory Leaks using ViewBinding inside Fragments.
                Override the onDestroyView method and set your binding instance to null inside it.
            """.trimIndent(),
            category = Category.CUSTOM_LINT_CHECKS,
            priority = 9,
            severity = Severity.WARNING,
            androidSpecific = true,
            implementation = Implementation(
                ViewBindingLeaksDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UClass::class.java)

    override fun applicableSuperClasses() = listOf(FRAGMENT_CLASS)

    override fun visitClass(context: JavaContext, declaration: UClass) {
        super.visitClass(context, declaration)

        findViewBindingFields(context, declaration).forEach { field: UField ->
            val onDestroyMethod: JvmMethod? = declaration.findMethodsByName(ON_DESTROY_VIEW_METHOD_NAME).firstOrNull()
            if (onDestroyMethod == null) {
                report(context, declaration)
            } else {
                UastFacade.getMethodBody(onDestroyMethod as PsiMethod)?.let { uExpression: UExpression ->
                    val regex = "${field.name}(.*)+=(.*)null".toRegex()
                    if (uExpression.asSourceString().contains(regex).not()) {
                        report(context, declaration)
                    }
                }
            }
        }
    }

    private fun findViewBindingFields(context: JavaContext, declaration: UClass): List<UField> {
        val evaluator = context.evaluator
        val fields = arrayListOf<UField>()
        declaration.fields.forEach { field: UField ->
            val type = field.type
            if (type is PsiClassType) {
                type.resolve()?.let { psiClass: PsiClass ->
                    if (evaluator.implementsInterface(psiClass, VIEW_BINDING_CLASS)) {
                        fields.add(field)
                    }
                }
            }
        }
        return fields
    }

    private fun report(context: JavaContext, declaration: UClass, debug: String = "") {
        context.report(
            issue = ISSUE,
            declaration,
            context.getNameLocation(declaration),
            message = "Be careful about MemoryLeaks using ViewBinding inside a Fragment. $debug"
        )
    }
}