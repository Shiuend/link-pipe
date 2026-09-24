package com.linkpipe.app.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(private val firestore: FirebaseFirestore) {
    private fun bookmarks(uid: String) = firestore.collection("users").document(uid).collection("bookmarks")

    fun observeBookmarks(uid: String): Flow<List<BookmarkWithMeta>> = callbackFlow {
        val listener = bookmarks(uid)
            .whereEqualTo("hidden", false)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().mapNotNull { document ->
                    document.toObject(Bookmark::class.java)?.let {
                        BookmarkWithMeta(it.copy(id = document.id, urlHash = it.urlHash.ifBlank { document.id }), document.metadata.hasPendingWrites())
                    }
                }.sortedByDescending { it.bookmark.createdAt }
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    fun observeBookmark(uid: String, urlHash: String): Flow<BookmarkWithMeta?> = callbackFlow {
        val listener = bookmarks(uid).document(urlHash)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val bookmark = snapshot?.takeIf { it.exists() }?.toObject(Bookmark::class.java)
                trySend(bookmark?.let { BookmarkWithMeta(it.copy(id = urlHash, urlHash = urlHash), snapshot.metadata.hasPendingWrites()) })
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveShared(uid: String, url: String, urlHash: String, source: String) {
        val payload = sharedBookmarkPayload(url, urlHash, source)
        bookmarks(uid).document(urlHash).set(payload, SetOptions.merge()).await()
    }

    suspend fun updateNote(uid: String, urlHash: String, note: String) = merge(uid, urlHash, mapOf("note" to note))

    suspend fun updateStatus(uid: String, urlHash: String, status: String) = merge(uid, urlHash, mapOf("status" to status))

    suspend fun hide(uid: String, urlHash: String) = merge(uid, urlHash, mapOf("hidden" to true))

    private suspend fun merge(uid: String, urlHash: String, values: Map<String, Any>) {
        bookmarks(uid).document(urlHash)
            .set(values + ("updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
            .await()
    }
}

internal fun sharedBookmarkPayload(url: String, urlHash: String, source: String): Map<String, Any> = mapOf(
    "id" to urlHash,
    "url" to url,
    "urlHash" to urlHash,
    "source" to source,
    "lastSharedBy" to "android",
    "status" to "unread",
    "hidden" to false,
    "createdAt" to Timestamp.now(),
    "updatedAt" to FieldValue.serverTimestamp(),
)
