package com.nexus.certifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.AnalyticsTracker
import com.nexus.common.Constants
import com.nexus.data.repository.CertificationRepository
import com.nexus.data.repository.UserXpRepository
import com.nexus.domain.model.Certification
import com.nexus.domain.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CertificationViewModel @Inject constructor(
    private val repository: CertificationRepository,
    private val xpRepository: UserXpRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Certification>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Certification>>> = _uiState.asStateFlow()

    init {
        analyticsTracker.track("screen_view", mapOf("screen" to "certifications"))
        loadCertifications()
    }

    private fun loadCertifications() {
        viewModelScope.launch {
            repository.getAllCertifications()
                .map<List<Certification>, UiState<List<Certification>>> { UiState.Success(it) }
                .catch { e -> emit(UiState.Error(e.message ?: "Failed to load certifications")) }
                .collect { _uiState.value = it }
        }
    }

    fun refresh() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.refreshCertifications()
            _uiState.value = when (result) {
                is com.nexus.common.ApiResult.Success -> UiState.Success(result.data)
                is com.nexus.common.ApiResult.Error -> UiState.Error(result.message)
                is com.nexus.common.ApiResult.Loading -> UiState.Loading
            }
        }
    }

    fun startCertification(certificationId: String, estimatedHours: Int) {
        viewModelScope.launch {
            repository.startCertification(certificationId, estimatedHours)
            analyticsTracker.track("certification_started", mapOf("certification_id" to certificationId))
        }
    }

    fun completeModule(certificationId: String) {
        viewModelScope.launch {
            val progress = repository.completeModule(certificationId) ?: return@launch
            xpRepository.awardXP(Constants.XP_PER_CERT_MODULE, "cert_module_completed")
            analyticsTracker.track("cert_module_completed", mapOf("certification_id" to certificationId))
            if (progress.isCompleted) {
                xpRepository.awardXP(Constants.XP_PER_CERTIFICATION, "certification_earned")
                analyticsTracker.track("certification_earned", mapOf("certification_id" to certificationId))
            }
        }
    }
}
