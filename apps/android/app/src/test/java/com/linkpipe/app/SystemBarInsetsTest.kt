package com.linkpipe.app

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemBarInsetsTest {
    @Test
    fun `root scaffold does not duplicate child system bar insets`() {
        val screen = File("src/main/java/com/linkpipe/app/LinkPipeApp.kt").readText()

        assertTrue(
            "The root scaffold must leave system bar insets to each destination scaffold",
            Regex("Scaffold\\s*\\(\\s*contentWindowInsets = WindowInsets\\(0\\)").containsMatchIn(screen),
        )
        assertEquals(
            "Only the root scaffold may disable default system bar insets",
            1,
            Regex("contentWindowInsets = WindowInsets\\(0\\)").findAll(screen).count(),
        )
        assertEquals(
            "Every toolbar destination must retain a TopAppBar with its default status bar inset",
            3,
            Regex("TopAppBar\\s*\\(").findAll(screen).count(),
        )
        assertFalse(
            "Toolbar destinations must not override the default TopAppBar status bar inset",
            "windowInsets =" in screen,
        )
    }
}
