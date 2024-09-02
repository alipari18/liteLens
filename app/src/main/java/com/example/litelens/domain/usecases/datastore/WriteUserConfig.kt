package com.example.litelens.domain.usecases.datastore

import com.example.litelens.domain.repository.datastore.LocalUserConfigManager

class WriteUserConfig(
    private val localUserConfigManager: LocalUserConfigManager
) {

    /**
     * Invokes the write operation to persist the user's configuration settings.
     *
     * The user configuration settings are written to the underlying data storage mechanism.
     */
    suspend operator fun invoke() {
        localUserConfigManager.writeUserConfig()
    }
}