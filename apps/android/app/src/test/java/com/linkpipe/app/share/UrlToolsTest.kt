package com.linkpipe.app.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlToolsTest {
    @Test fun `extracts first URL from mixed text`() {
        assertEquals("https://example.com/article", UrlTools.extractFirstUrl("See https://example.com/article now"))
    }

    @Test fun `returns null when no URL exists`() {
        assertNull(UrlTools.extractFirstUrl("nothing to save"))
    }

    @Test fun `normalizes host port fragment slash and query`() {
        assertEquals(
            "https://example.com/Path?a=2&z=1",
            UrlTools.normalize("HTTPS://Example.COM:443/Path/?z=1&a=2#part"),
        )
    }

    @Test fun `hash is stable lowercase sha256`() {
        assertEquals(
            "632538290468e7a39c06323c9e3ae98f31072d641cbb37ea37917f56bbeb5539",
            UrlTools.sha256("https://example.com/article"),
        )
    }

    @Test fun `matches shared normalization fixtures`() {
        val fixture = checkNotNull(javaClass.classLoader?.getResource("url-normalization-fixtures.json"))
            .readText()
        val cases = Regex(
            """\{\s*"raw"\s*:\s*"([^"]+)",\s*"normalized"\s*:\s*"([^"]+)",\s*"sha256"\s*:\s*"([a-f0-9]{64})"\s*}""",
        ).findAll(fixture).toList()

        assertTrue("Expected at least one shared URL fixture", cases.isNotEmpty())
        cases.forEach { match ->
            val (raw, normalized, hash) = match.destructured
            assertEquals(normalized, UrlTools.normalize(raw))
            assertEquals(hash, UrlTools.sha256(normalized))
        }
    }
}
