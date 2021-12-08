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
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UastFacade

@Beta
class ViewBindingLeaksDetector : Detector(), Detector.UastScanner, SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UClass::class.java)

    override fun applicableSuperClasses() = listOf("androidx.fragment.app.Fragment")

    override fun visitClass(context: JavaContext, declaration: UClass) {
        super.visitClass(context, declaration)

        val field: PsiField? = declaration.findFieldByName("_binding", true)
        if (field != null) {
            val onDestroyMethod: JvmMethod? = declaration.findMethodsByName("onDestroyView").firstOrNull()
            if (onDestroyMethod == null) {
                context.report(
                    issue = ISSUE,
                    declaration,
                    context.getNameLocation(declaration),
                    message = "Be careful about MemoryLeaks using ViewBinding."
                )
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

    private fun report(context: JavaContext, declaration: UClass) {
        context.report(
            issue = ISSUE,
            declaration,
            context.getNameLocation(declaration),
            message = "Be careful about MemoryLeaks using ViewBinding."
        )
    }

    companion object {
        private val IMPLEMENTATION = Implementation(
            ViewBindingLeaksDetector::class.java,
            Scope.JAVA_FILE_SCOPE
        )

        val ISSUE: Issue = Issue.create(
            id = "ViewBindingLeaksDetector",
            briefDescription = "Be careful about MemoryLeaks using ViewBindings",
            explanation = """
                You should be careful about Memory Leaks using ViewBinding inside Fragments.
                Override the onDestroyView method and set your binding instance to null inside it.
            """.trimIndent(),
            category = Category.CUSTOM_LINT_CHECKS,
            priority = 9,
            severity = Severity.WARNING,
            androidSpecific = true,
            implementation = IMPLEMENTATION
        )
    }
}