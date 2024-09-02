package com.example.litelens.data.utils

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.example.litelens.utils.Constants

object PreferenceDatastoreKeys {
    val USER_CONFIG = booleanPreferencesKey(name = Constants.USER_CONFIG)
}