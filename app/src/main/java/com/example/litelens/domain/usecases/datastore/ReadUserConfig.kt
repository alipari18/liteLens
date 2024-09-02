package com.example.litelens.domain.usecases.datastore

import com.example.litelens.domain.repository.datastore.LocalUserConfigManager
import kotlinx.coroutines.flow.Flow

class ReadUserConfig(
    private val localUserConfigManager: LocalUserConfigManager
) {

    /**
     * Invokes the read operation to obtain the user's configuration settings as a Flow.
     *
     * The returned Flow emits the current user configuration settings allowing
     */
    operator fun invoke(): Flow<Boolean> {
        return localUserConfigManager.readUserConfig()
    }
}