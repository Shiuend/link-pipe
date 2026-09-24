package com.linkpipe.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.linkpipe.app.data.BookmarkWithMeta
import java.text.DateFormat

private const val SIGN_IN = "signIn"
private const val LIST = "bookmarks"
private const val SETTINGS = "settings"
private const val DETAIL = "bookmarkDetail/{urlHash}"

@Composable
fun LinkPipeApp(viewModel: AppViewModel) {
    val auth by viewModel.authState.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val error by viewModel.error.collectAsStateWithLifecycle()
    val notice by viewModel.notice.collectAsStateWithLifecycle()
    val savedShareHash by viewModel.savedShareHash.collectAsStateWithLifecycle()

    LaunchedEffect(auth) {
        when (auth) {
            AuthState.SignedOut -> nav.navigate(SIGN_IN) { popUpTo(0) }
            is AuthState.SignedIn -> nav.navigate(LIST) { popUpTo(0) }
            AuthState.Loading -> Unit
        }
    }
    LaunchedEffect(error, notice) {
        (error ?: notice)?.let { snackbar.showSnackbar(it) }
        viewModel.clearError()
        viewModel.clearNotice()
    }
    LaunchedEffect(savedShareHash) {
        savedShareHash?.let {
            nav.navigate("bookmarkDetail/$it")
            viewModel.clearSavedShareHash()
        }
    }

    MaterialTheme {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            snackbarHost = { SnackbarHost(snackbar) },
        ) { padding ->
            if (auth == AuthState.Loading) {
                Box(Modifier.fillMaxSize().padding(padding)) { CircularProgressIndicator() }
            } else {
                NavHost(nav, startDestination = if (auth is AuthState.SignedIn) LIST else SIGN_IN, Modifier.padding(padding)) {
                    composable(SIGN_IN) { SignInScreen(viewModel) }
                    composable(LIST) {
                        BookmarkListScreen(viewModel, onSettings = { nav.navigate(SETTINGS) }) {
                            nav.navigate("bookmarkDetail/$it")
                        }
                    }
                    composable(SETTINGS) { SettingsScreen(viewModel, auth, nav::popBackStack) }
                    composable(DETAIL, arguments = listOf(navArgument("urlHash") { type = NavType.StringType })) { entry ->
                        BookmarkDetailScreen(viewModel, entry.arguments?.getString("urlHash").orEmpty(), nav::popBackStack)
                    }
                }
            }
        }
    }
}

@Composable
private fun SignInScreen(viewModel: AppViewModel) {
    val activity = LocalActivity.current
    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.sign_in_tagline))
        Spacer(Modifier.height(24.dp))
        Button(onClick = { activity?.let(viewModel::signIn) }, enabled = activity != null) { Text(stringResource(R.string.sign_in_with_google)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarkListScreen(viewModel: AppViewModel, onSettings: () -> Unit, onOpen: (String) -> Unit) {
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.bookmarks_title)) }, actions = {
            IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, stringResource(R.string.settings_title)) }
        })
    }) { padding ->
        if (bookmarks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(24.dp)) { Text(stringResource(R.string.no_saved_links)) }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(bookmarks, key = { it.bookmark.urlHash }) { item -> BookmarkRow(item) { onOpen(item.bookmark.urlHash) } }
            }
        }
    }
}

@Composable
private fun BookmarkRow(item: BookmarkWithMeta, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                item.bookmark.url,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (item.bookmark.status == "unread") FontWeight.Bold else FontWeight.Normal,
            )
            if (item.isSyncing) CircularProgressIndicator(Modifier.padding(start = 8.dp))
        }
        if (item.bookmark.note.isNotBlank()) {
            Text(item.bookmark.note, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarkDetailScreen(viewModel: AppViewModel, hash: String, onBack: () -> Unit) {
    val detailFlow = remember(hash) { viewModel.observeDetail(hash) }
    val item by detailFlow.collectAsStateWithLifecycle(initialValue = null)
    val bookmark = item?.bookmark
    val context = LocalContext.current
    var note by remember { mutableStateOf("") }
    var originalNote by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(bookmark?.note, editing) {
        if (!editing) {
            note = bookmark?.note.orEmpty()
            originalNote = bookmark?.note.orEmpty()
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.bookmark_detail_title)) }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
        }, actions = {
            IconButton(onClick = {
                bookmark?.url?.let {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
                    catch (_: ActivityNotFoundException) { viewModel.error.value = context.getString(R.string.browser_not_found) }
                }
            }) { Icon(Icons.Default.OpenInBrowser, stringResource(R.string.open_link)) }
            IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, stringResource(R.string.delete)) }
        })
    }) { padding ->
        if (bookmark == null) {
            Box(Modifier.fillMaxSize().padding(padding).padding(24.dp)) { Text(stringResource(R.string.bookmark_not_found)) }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(bookmark.url, maxLines = 1, overflow = TextOverflow.Ellipsis)
                TextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth().onFocusChanged { state ->
                        if (editing && !state.isFocused && note != originalNote) {
                            viewModel.updateNote(hash, note)
                            originalNote = note
                        }
                        editing = state.isFocused
                    },
                    minLines = 3,
                    placeholder = { Text(stringResource(R.string.add_note)) },
                    label = { Text(stringResource(R.string.note)) },
                )
                Text(stringResource(R.string.source_format, bookmark.source.ifBlank { context.getString(R.string.unknown) }))
                Text(stringResource(R.string.created_at_format, bookmark.createdAt?.toDate()?.let(DateFormat.getDateTimeInstance()::format) ?: stringResource(R.string.date_unavailable)))
                if (item?.isSyncing == true) Text(stringResource(R.string.syncing))
                Button(onClick = { viewModel.toggleStatus(hash, bookmark.status) }) {
                    Text(stringResource(if (bookmark.status == "read") R.string.mark_unread else R.string.mark_read))
                }
            }
        }
    }
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text(stringResource(R.string.delete_bookmark_title)) },
        text = { Text(stringResource(R.string.delete_bookmark_message)) },
        confirmButton = { TextButton(onClick = { confirmDelete = false; viewModel.hide(hash, onBack) }) { Text(stringResource(R.string.delete)) } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(viewModel: AppViewModel, auth: AuthState, onBack: () -> Unit) {
    val activity = LocalActivity.current
    val shareBehavior by viewModel.shareBehavior.collectAsStateWithLifecycle()
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.settings_title)) }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (auth is AuthState.SignedIn) {
                Text(auth.name.ifBlank { stringResource(R.string.google_account) }, style = MaterialTheme.typography.titleMedium)
                Text(auth.email)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.quick_save_title))
                    Text(stringResource(R.string.quick_save_description), style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = shareBehavior.quickSave, onCheckedChange = viewModel::setQuickSave)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.open_detail_title))
                    Text(stringResource(R.string.open_detail_description), style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = shareBehavior.openDetail, onCheckedChange = viewModel::setOpenDetail)
            }
            Button(onClick = { activity?.let(viewModel::signOut) }) { Text(stringResource(R.string.sign_out)) }
        }
    }
}
