package me.snoty.integration.plugin.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PackageUtilsTest {
    private fun groupCommonPackages(vararg packageNames: String) = groupCommonPackages(packageNames.toList())

    @Test
    fun `test package name commonization simple parent`() {
        assertEquals(
            listOf("me.snoty.myintegration"),
            groupCommonPackages("me.snoty.myintegration.mynode", "me.snoty.myintegration.myothernode")
        )
    }

    @Test
    fun `test package name commonization single package`() {
        assertEquals(
            listOf("me.snoty.myintegration"),
            groupCommonPackages("me.snoty.myintegration.mynode")
        )
    }

    @Test
    fun `test package name commonization no common parent`() {
        assertEquals(
            listOf("me.snoty.myintegration", "me.simulatan.myintegration"),
            groupCommonPackages("me.snoty.myintegration.mynode", "me.simulatan.myintegration.myothernode")
        )

        assertEquals(
            listOf("me.simulatan", "me.simulatan.snoty.myintegration"),
            groupCommonPackages("me.simulatan.mynode", "me.simulatan.snoty.myintegration.myothernode")
        )
    }
}
