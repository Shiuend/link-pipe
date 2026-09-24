package com.linkpipe.app

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.linkpipe.app.data.BookmarkRepository
import com.linkpipe.app.data.BookmarkWithMeta
import com.linkpipe.app.data.ShareBehavior
import com.linkpipe.app.data.UserPreferences
import com.linkpipe.app.share.UrlTools
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class SharedPayload(val text: String, val source: String)
const val EXTRA_SHARE_SOURCE = "com.linkpipe.app.extra.SHARE_SOURCE"

internal fun buildGoogleSignInRequest(clientId: String): GetCredentialRequest {
    val option = GetSignInWithGoogleOption.Builder(clientId).build()
    return GetCredentialRequest.Builder().addCredentialOption(option).build()
}

sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val uid: String, val name: String, val email: String) : AuthState
}

sealed interface ShareSaveState {
    data object Idle : ShareSaveState
    data object Saving : ShareSaveState
    data object Saved : ShareSaveState
    data class Failed(val message: String) : ShareSaveState
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val repository: BookmarkRepository,
    private val preferences: UserPreferences,
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState
    val error = MutableStateFlow<String?>(null)
    val notice = MutableStateFlow<String?>(null)
    val savedShareHash = MutableStateFlow<String?>(null)
    val shareSaveState = MutableStateFlow<ShareSaveState>(ShareSaveState.Idle)
    private var pendingShare: SharedPayload? = null

    val shareBehavior = preferences.shareBehavior.stateIn(viewModelScope, SharingStarted.Eagerly, ShareBehavior())
    val bookmarks: StateFlow<List<BookmarkWithMeta>> = authState.flatMapLatest { state ->
        if (state is AuthState.SignedIn) repository.observeBookmarks(state.uid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _authState.value = if (user == null) AuthState.SignedOut else AuthState.SignedIn(
                uid = user.uid,
                name = user.displayName.orEmpty(),
                email = user.email.orEmpty(),
            )
            if (user != null) pendingShare?.let { consumeShare(it) }
        }
    }

    fun signIn(activity: Activity) = viewModelScope.launch {
        error.value = null
        try {
            val clientId = activity.getString(R.string.default_web_client_id)
            require(!clientId.startsWith("REPLACE_")) { context.getString(R.string.firebase_client_id_missing) }
            val request = buildGoogleSignInRequest(clientId)
            val result = CredentialManager.create(activity).getCredential(activity, request)
            val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            auth.signInWithCredential(GoogleAuthProvider.getCredential(googleCredential.idToken, null)).await()
        } catch (exception: Exception) {
            error.value = exception.message ?: context.getString(R.string.sign_in_failed)
        }
    }

    fun signOut(activity: Activity) = viewModelScope.launch {
        auth.signOut()
        runCatching { CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest()) }
    }

    fun receiveShare(payload: SharedPayload) {
        pendingShare = payload
        if (auth.currentUser != null) consumeShare(payload)
    }

    private fun consumeShare(payload: SharedPayload) = viewModelScope.launch {
        shareSaveState.value = ShareSaveState.Saving
        val raw = UrlTools.extractFirstUrl(payload.text)
        if (raw == null) {
            val message = context.getString(R.string.shared_url_not_found)
            error.value = message
            shareSaveState.value = ShareSaveState.Failed(message)
            pendingShare = null
            return@launch
        }
        runCatching {
            val normalized = UrlTools.normalize(raw)
            val hash = UrlTools.sha256(normalized)
            repository.saveShared(requireUid(), normalized, hash, payload.source)
            pendingShare = null
            notice.value = context.getString(R.string.link_saved)
            if (shareBehavior.value.openDetail) savedShareHash.value = hash
            shareSaveState.value = ShareSaveState.Saved
            hash
        }.onFailure {
            val message = it.message ?: context.getString(R.string.save_link_failed)
            error.value = message
            shareSaveState.value = ShareSaveState.Failed(message)
        }
    }

    fun observeDetail(hash: String): Flow<BookmarkWithMeta?> = repository.observeBookmark(requireUid(), hash)
    fun updateNote(hash: String, note: String) = launchWrite { repository.updateNote(requireUid(), hash, note) }
    fun toggleStatus(hash: String, current: String) = launchWrite {
        repository.updateStatus(requireUid(), hash, if (current == "read") "unread" else "read")
    }
    fun hide(hash: String, after: () -> Unit) = launchWrite { repository.hide(requireUid(), hash); after() }
    fun setQuickSave(enabled: Boolean) = launchWrite { preferences.setQuickSaveOnShare(enabled) }
    fun setOpenDetail(enabled: Boolean) = launchWrite { preferences.setOpenDetailOnShare(enabled) }
    fun clearNotice() { notice.value = null }
    fun clearError() { error.value = null }
    fun clearSavedShareHash() { savedShareHash.value = null }

    private fun requireUid(): String = auth.currentUser?.uid ?: error(context.getString(R.string.not_signed_in))
    private fun launchWrite(block: suspend () -> Unit) = viewModelScope.launch {
        runCatching { block() }.onFailure { error.value = it.message ?: context.getString(R.string.operation_failed) }
    }
}
