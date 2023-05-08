package com.ludovic.vimont.lintrules

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.google.common.annotations.Beta
import org.junit.Test
import java.io.File

@Beta
class ViewBindingLeaksDetectorTest {
    companion object {
        private const val RESOURCE_FILE_PATH = "src/test/resources"
    }

    private val viewBindingStub = kotlin("""
        package androidx.viewbinding
        class ViewBinding
    """).indented()

    private val fragmentStub = kotlin("""
        package androidx.fragment.app
        class Fragment
    """).indented()

    private val myBindingStub = kotlin("""
        package com.ludovic.vimont.customlintrules
        abstract class MyBinding: androidx.viewbinding.ViewBinding
    """).indented()

    @Test
    fun `test memory leaks detection`() {
        // Given
        val fileContent = String(File("$RESOURCE_FILE_PATH/memory_leaks.txt").readBytes())

        // When
        val testLintResult = lint().files(fragmentStub, viewBindingStub, myBindingStub, kotlin(fileContent).indented())
            .issues(VIEW_BINDING_ISSUE)
            .allowCompilationErrors()
            .run()

        // Then
        testLintResult.expectWarningCount(1)
    }

    @Test
    fun `test no memory leaks are raised while resetting binding as expected`() {
        // Given
        val fileContent = String(File("$RESOURCE_FILE_PATH/no_memory_leaks.txt").readBytes())

        // When
        val testLintResult = lint().files(fragmentStub, viewBindingStub, myBindingStub, kotlin(fileContent).indented())
            .issues(VIEW_BINDING_ISSUE)
            .allowCompilationErrors()
            .run()

        // Then
        testLintResult.expectWarningCount(0)
    }
}