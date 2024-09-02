package com.example.litelens.data.manager.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.litelens.data.utils.PreferenceDatastoreKeys
import com.example.litelens.domain.repository.datastore.LocalUserConfigManager
import com.example.litelens.utils.extension.datastore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalUserConfigManagerImpl @Inject constructor(
    private val context: Context
): LocalUserConfigManager{

    override suspend fun writeUserConfig() {
        // Using extension function obtain instance of 'userConfigDatastore' to write value
        context.datastore.edit { userConfigDatastore ->
            userConfigDatastore[PreferenceDatastoreKeys.USER_CONFIG] = true
        }
    }

    override fun readUserConfig(): Flow<Boolean> {
        // Using extension function obtain instance of 'userConfigDatastore' to read keys
        return context.datastore.data
            .map { preferences ->
                preferences[PreferenceDatastoreKeys.USER_CONFIG] ?: false
            }
    }
}