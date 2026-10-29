package com.nexus.data.usecase

import android.content.Context
import android.net.Uri
import com.nexus.database.NexusDatabase
import com.nexus.database.dao.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.OutputStream
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: NexusDatabase,
) {
    suspend fun exportToStream(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val outputStream: OutputStream = context.contentResolver.openOutputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open output stream"))

            val export = buildExportJson()

            outputStream.bufferedWriter().use { writer ->
                writer.write(export.toString(2))
            }

            Timber.d("Data export completed to $uri")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Data export failed")
            Result.failure(e)
        }
    }

    private suspend fun buildExportJson(): JSONObject {
        val export = JSONObject()
        export.put("exportedAt", Instant.now().toString())
        export.put("schema", "nexus-user-data-v1")

        val userData = JSONObject()

        userData.put("applications", readAll { database.applicationDao().getAllEntities() })
        userData.put("achievements", readAll { database.achievementDao().getAllEntities() })
        userData.put("goals", readAll { database.goalDao().getAllEntities() })
        userData.put("studySessions", readAll { database.studySessionDao().getAllEntities() })
        userData.put("dsaProblems", readAll { database.dsaProblemDao().getAllEntities() })
        userData.put("interviewOutcomes", readAll { database.interviewOutcomeDao().getAllEntities() })
        userData.put("confidenceAssessments", readAll { database.confidenceAssessmentDao().getAllEntities() })
        userData.put("careerMirrors", readAll { database.careerMirrorDao().getAllEntities() })
        userData.put("scoreSnapshots", readAll { database.scoreSnapshotDao().getAllEntities() })
        userData.put("skillVelocity", readAll { database.skillVelocitySnapshotDao().getAllEntities() })
        userData.put("interviewReports", readAll { database.interviewReportDao().getAllEntities() })
        userData.put("placementForecasts", readAll { database.placementForecastDao().getAllEntities() })
        userData.put("notifications", readAll { database.notificationDao().getAllEntities() })
        userData.put("aiConversations", readAll { database.aiDao().getAllEntities() })
        userData.put("careerMemories", readAll { database.careerMemoryDao().getAllEntities() })

        export.put("userData", userData)
        return export
    }

    private suspend fun readAll(readOperation: suspend () -> List<*>): JSONArray {
        return try {
            val items = readOperation()
            JSONArray(items.map { item ->
                val json = JSONObject()
                item?.javaClass?.declaredFields?.forEach { field ->
                    try {
                        field.isAccessible = true
                        val value = field.get(item)
                        if (value != null) {
                            json.put(field.name, value.toString())
                        }
                    } catch (_: Exception) { }
                }
                json
            })
        } catch (e: Exception) {
            Timber.w(e, "Failed to read data for export")
            JSONArray()
        }
    }
}
