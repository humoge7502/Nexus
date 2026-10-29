package com.nexus.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.AnalyticsTracker
import com.nexus.database.dao.NotificationDao
import com.nexus.database.entity.NotificationEntity
import com.nexus.domain.model.NotificationItem
import com.nexus.domain.model.NotificationType
import com.nexus.domain.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val dao: NotificationDao,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<NotificationItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<NotificationItem>>> = _uiState.asStateFlow()

    init {
        analyticsTracker.track("screen_view", mapOf("screen" to "notifications"))
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            dao.getAll()
                .map<List<NotificationEntity>, UiState<List<NotificationItem>>> { entities ->
                    UiState.Success(entities.map { it.toDomain() })
                }
                .catch { e -> emit(UiState.Error(e.message ?: "Failed to load notifications")) }
                .collect { _uiState.value = it }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch { dao.markAsRead(id) }
    }

    fun markAllAsRead() {
        viewModelScope.launch { dao.markAllAsRead() }
    }

    private fun NotificationEntity.toDomain() = NotificationItem(
        id = id,
        type = try { NotificationType.valueOf(type) } catch (_: Exception) { NotificationType.SYSTEM },
        title = title,
        body = body,
        isRead = isRead,
        actionUrl = actionUrl,
        createdAt = Instant.ofEpochMilli(createdAt),
    )
}
