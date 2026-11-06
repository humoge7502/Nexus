package com.nexus.data.usecase

import android.content.Context
import com.nexus.database.NexusDatabase
import com.nexus.datastore.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Removes the device-side record of the student. The remote deletion in
 * [AccountDeletionViewModel] is only half the promise the store page makes —
 * "the deletion is real, it removes your data rather than hiding it" — and
 * this is the other half: Room tables, DataStore keys and the app's own
 * files all go, so a reinstalled-and-signed-in-again device starts empty.
 *
 * Backup files created by the OS outside the app's sandbox are out of scope;
 * they are the platform's business, not this method's.
 */
@Singleton
class LocalDataWiper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: NexusDatabase,
    private val userPreferences: UserPreferences,
) {
    suspend fun wipe() = withContext(Dispatchers.IO) {
        runCatching { database.clearAllTables() }
            .onFailure { Timber.w(it, "Room clearAllTables failed during local wipe") }

        runCatching { userPreferences.clearAll() }
            .onFailure { Timber.w(it, "DataStore clear failed during local wipe") }

        clearContents(context.filesDir)
        clearContents(context.cacheDir)
    }

    private fun clearContents(dir: File?) {
        dir?.listFiles()?.forEach { child ->
            runCatching {
                if (child.isDirectory) child.deleteRecursively() else child.delete()
            }.onFailure { Timber.w(it, "Local wipe could not remove %s", child.name) }
        }
    }
}
