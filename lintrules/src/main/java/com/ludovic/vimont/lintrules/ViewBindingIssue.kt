package com.ludovic.vimont.lintrules

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity

val VIEW_BINDING_ISSUE = Issue.create(
    id = "ViewBindingLeaksDetector",
    briefDescription = "Be careful about memory leaks while using ViewBinding!",
    explanation = """
        You should be cautious when using ViewBinding inside Fragments, it can cause memory leaks.
        You need to override the onDestroyView method and set your binding instance to null inside it.
        Otherwise, memory leaks could occurred!
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