package com.example.litelens.domain.repository.datastore

import kotlinx.coroutines.flow.Flow

interface LocalUserConfigManager {
    suspend fun writeUserConfig()
    fun readUserConfig(): Flow<Boolean>
}