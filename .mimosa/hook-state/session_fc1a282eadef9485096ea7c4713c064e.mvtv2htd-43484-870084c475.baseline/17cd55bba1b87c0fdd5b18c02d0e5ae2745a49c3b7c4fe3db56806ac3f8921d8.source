package com.nexus.sync

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.firestore.FirebaseFirestore
import com.nexus.common.ApiResult
import com.nexus.common.NetworkMonitor
import com.nexus.database.dao.CertificationDao
import com.nexus.database.dao.GoalDao
import com.nexus.database.dao.HackathonDao
import com.nexus.database.dao.InternshipDao
import com.nexus.database.entity.CertificationEntity
import com.nexus.database.entity.GoalEntity
import com.nexus.database.entity.HackathonEntity
import com.nexus.database.entity.InternshipEntity
import com.nexus.data.repository.CertificationRepository
import com.nexus.data.repository.GoalRepository
import com.nexus.data.repository.HackathonRepository
import com.nexus.data.repository.InternshipRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.syncStore by preferencesDataStore(name = "firestore_sync_prefs")

@Singleton
class FirestoreSync @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val internshipRepository: InternshipRepository,
    private val hackathonRepository: HackathonRepository,
    private val certificationRepository: CertificationRepository,
    private val goalRepository: GoalRepository,
    private val networkMonitor: NetworkMonitor,
    private val internshipDao: InternshipDao,
    private val hackathonDao: HackathonDao,
    private val certificationDao: CertificationDao,
    private val goalDao: GoalDao,
) {
    suspend fun syncAll() {
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) {
            Timber.d("FirestoreSync: offline, skipping")
            return
        }

        val results = mapOf(
            "internships" to syncInternships(),
            "hackathons" to syncHackathons(),
            "certifications" to syncCertifications(),
            "goals" to syncGoals(),
        )

        val failures = results.filterValues { it is ApiResult.Error }
        if (failures.isNotEmpty()) {
            Timber.w("Firestore sync had failures: ${failures.keys}")
            syncFromFirestore(failures.keys.toList())
        } else {
            Timber.d("Firestore sync all completed successfully")
        }
    }

    private suspend fun syncInternships(): ApiResult<*> {
        return try {
            internshipRepository.refreshInternships()
        } catch (e: Exception) {
            Timber.e(e, "Internship API sync failed")
            ApiResult.Error(e.message ?: "API sync failed")
        }
    }

    private suspend fun syncHackathons(): ApiResult<*> {
        return try {
            hackathonRepository.refreshHackathons()
        } catch (e: Exception) {
            Timber.e(e, "Hackathon API sync failed")
            ApiResult.Error(e.message ?: "API sync failed")
        }
    }

    private suspend fun syncCertifications(): ApiResult<*> {
        return try {
            certificationRepository.refreshCertifications()
        } catch (e: Exception) {
            Timber.e(e, "Certification API sync failed")
            ApiResult.Error(e.message ?: "API sync failed")
        }
    }

    private suspend fun syncGoals(): ApiResult<*> {
        return try {
            goalRepository.refreshGoals()
        } catch (e: Exception) {
            Timber.e(e, "Goal API sync failed")
            ApiResult.Error(e.message ?: "API sync failed")
        }
    }

private suspend fun syncFromFirestore(failedCollections: List<String>) {
  Timber.d("Falling back to Firestore for: $failedCollections")
  val typeMap = mapOf(
    "internships" to "INTERNSHIP",
    "hackathons" to "HACKATHON",
    "certifications" to "CERTIFICATION",
  )
  val grouped = failedCollections.mapNotNull { typeMap[it] }.distinct()
  if (grouped.isEmpty()) return
  try {
    val snapshot = firestore.collection("opportunities")
      .whereIn("type", grouped)
      .get()
      .await()
    if (!snapshot.isEmpty) {
      val docs = snapshot.documents
      val internships = docs.filter { it.getString("type") == "INTERNSHIP" }
      val hackathons = docs.filter { it.getString("type") == "HACKATHON" }
      val certifications = docs.filter { it.getString("type") == "CERTIFICATION" }
      if (internships.isNotEmpty()) {
        val entities = internships.mapNotNull { doc ->
          doc.toObject(com.nexus.database.entity.InternshipEntity::class.java)
        }
        if (entities.isNotEmpty()) internshipDao.insertAll(entities)
      }
      if (hackathons.isNotEmpty()) {
        val entities = hackathons.mapNotNull { doc ->
          doc.toObject(com.nexus.database.entity.HackathonEntity::class.java)
        }
        if (entities.isNotEmpty()) hackathonDao.insertAll(entities)
      }
      if (certifications.isNotEmpty()) {
        val entities = certifications.mapNotNull { doc ->
          doc.toObject(com.nexus.database.entity.CertificationEntity::class.java)
        }
        if (entities.isNotEmpty()) certificationDao.insertAll(entities)
      }
    }
    updateLastSyncTimestamp("opportunities")
    Timber.d(
      "Firestore fallback: synced $grouped from 'opportunities' collection " +
        "(${snapshot.size()} docs)"
    )
  } catch (e: Exception) {
    Timber.e(e, "Firestore fallback failed for: $failedCollections")
  }
}

    suspend fun getLastSyncTimestamp(collection: String): Long {
        val key = longPreferencesKey("sync_ts_$collection")
        return context.syncStore.data.map { prefs ->
            prefs[key] ?: 0L
        }.first()
    }

    suspend fun updateLastSyncTimestamp(collection: String) {
        val key = longPreferencesKey("sync_ts_$collection")
        context.syncStore.edit { prefs ->
            prefs[key] = System.currentTimeMillis()
        }
    }
}
