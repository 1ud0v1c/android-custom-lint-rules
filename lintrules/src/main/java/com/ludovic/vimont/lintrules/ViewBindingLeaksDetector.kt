package com.ludovic.vimont.lintrules

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.google.common.annotations.Beta
import com.google.common.annotations.VisibleForTesting
import com.intellij.lang.jvm.JvmMethod
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UField
import org.jetbrains.uast.UastFacade

@Beta
class ViewBindingLeaksDetector : Detector(), SourceCodeScanner {
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
            priority = 7,
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
                val message = "Don't forget to implements $ON_DESTROY_VIEW_METHOD_NAME inside a fragment " +
                        "to avoid memory leaks while using ViewBinding."
                report(context, declaration, message)
            } else {
                UastFacade.getMethodBody(onDestroyMethod as PsiMethod)?.let { uExpression: UExpression ->
                    val regex = "${field.name}(.*)+=(.*)null".toRegex()
                    if (uExpression.asSourceString().contains(regex).not()) {
                        val message = "You need to set the field: \"${field.name}\" to null to avoid memory leaks."
                        report(context, declaration, message, context.getLocation(uExpression))
                    }
                }
            }
        }
    }

    private fun findViewBindingFields(context: JavaContext, declaration: UClass): List<UField> {
        val evaluator = context.evaluator
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
            issue = ISSUE,
            scopeClass = declaration,
            location = location,
            message = message,
        )
    }
}