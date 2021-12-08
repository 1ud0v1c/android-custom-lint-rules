package com.ludovic.vimont.lintrules

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test
import java.io.File

class ViewBindingLeaksDetectorTest {
    companion object {
        private const val RESOURCE_FILE_PATH = "src/test/resources"
    }

    private val fragmentStub = kotlin("""
        package androidx.fragment.app
        class Fragment
    """).indented()

    @Test
    fun `test memory leaks detection`() {
        // Given
        val fileContent = String(File("$RESOURCE_FILE_PATH/memory_leaks.txt").readBytes())

        // When
        val testLintResult = lint().files(fragmentStub, kotlin(fileContent).indented())
            .issues(ViewBindingLeaksDetector.ISSUE)
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
        val testLintResult = lint().files(fragmentStub, kotlin(fileContent).indented())
            .issues(ViewBindingLeaksDetector.ISSUE)
            .allowCompilationErrors()
            .run()

        // Then
        testLintResult.expectWarningCount(0)
    }
}