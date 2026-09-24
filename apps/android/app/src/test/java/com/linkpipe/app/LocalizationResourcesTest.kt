package com.linkpipe.app

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class LocalizationResourcesTest {
    @Test
    fun `english and traditional chinese resources expose the same strings`() {
        val english = stringsIn("src/main/res/values/strings.xml").filterKeys { it != "default_web_client_id" }
        val traditionalChinese = stringsIn("src/main/res/values-zh-rTW/strings.xml")

        assertEquals(english.keys, traditionalChinese.keys)
        assertEquals("Bookmarks", english.getValue("bookmarks_title"))
        assertEquals("書籤", traditionalChinese.getValue("bookmarks_title"))
        assertEquals("—", english.getValue("date_unavailable"))
    }

    @Test
    fun `manifest uses the localized application name`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()

        assertTrue("android:label=\"@string/app_name\"" in manifest)
    }

    @Test
    fun `missing date marker is not hardcoded in the compose screen`() {
        val screen = File("src/main/java/com/linkpipe/app/LinkPipeApp.kt").readText()

        assertFalse("\"—\"" in screen)
    }

    private fun stringsIn(path: String): Map<String, String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(path))
        return document.getElementsByTagName("string").let { nodes ->
            (0 until nodes.length).associate { index ->
                val element = nodes.item(index) as Element
                element.getAttribute("name") to element.textContent
            }
        }
    }
}
