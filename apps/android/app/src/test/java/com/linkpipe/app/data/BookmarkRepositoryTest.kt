package com.linkpipe.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class BookmarkRepositoryTest {
    @Test
    fun `shared bookmark payload identifies Android as the client platform`() {
        val payload = sharedBookmarkPayload(
            url = "https://example.com/article",
            urlHash = "hash",
            source = "Chrome",
        )

        assertEquals("android", payload["lastSharedBy"])
    }
}
