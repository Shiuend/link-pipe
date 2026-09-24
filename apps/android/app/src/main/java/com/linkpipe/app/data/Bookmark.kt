package com.linkpipe.app.data

import com.google.firebase.Timestamp

data class Bookmark(
    val id: String = "",
    val url: String = "",
    val urlHash: String = "",
    val note: String = "",
    val source: String = "",
    val status: String = "unread",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val hidden: Boolean = false,
)

data class BookmarkWithMeta(
    val bookmark: Bookmark,
    val isSyncing: Boolean = false,
)
