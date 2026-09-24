package com.linkpipe.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("linkpipe_preferences")

data class ShareBehavior(
    val quickSave: Boolean = true,
    val openDetail: Boolean = false,
) {
    fun withQuickSave(enabled: Boolean) = copy(
        quickSave = enabled,
        openDetail = if (enabled) false else openDetail,
    )

    fun withOpenDetail(enabled: Boolean) = copy(
        quickSave = if (enabled) false else quickSave,
        openDetail = enabled,
    )
}

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext private val context: Context) {
    private val openDetailKey = booleanPreferencesKey("open_detail_on_share")
    private val quickSaveKey = booleanPreferencesKey("quick_save_on_share")
    val shareBehavior: Flow<ShareBehavior> = context.dataStore.data.map { values ->
        val openDetail = values[openDetailKey] ?: false
        ShareBehavior(quickSave = values[quickSaveKey] ?: !openDetail, openDetail = openDetail)
    }

    suspend fun setQuickSaveOnShare(enabled: Boolean) {
        context.dataStore.edit { values ->
            val currentOpenDetail = values[openDetailKey] ?: false
            val behavior = ShareBehavior(
                quickSave = values[quickSaveKey] ?: !currentOpenDetail,
                openDetail = currentOpenDetail,
            ).withQuickSave(enabled)
            values[quickSaveKey] = behavior.quickSave
            values[openDetailKey] = behavior.openDetail
        }
    }

    suspend fun setOpenDetailOnShare(enabled: Boolean) {
        context.dataStore.edit { values ->
            val currentOpenDetail = values[openDetailKey] ?: false
            val behavior = ShareBehavior(
                quickSave = values[quickSaveKey] ?: !currentOpenDetail,
                openDetail = currentOpenDetail,
            ).withOpenDetail(enabled)
            values[quickSaveKey] = behavior.quickSave
            values[openDetailKey] = behavior.openDetail
        }
    }
}
