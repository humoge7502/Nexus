package com.nexus.app.tour

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.datastore.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Lives on the shell rather than on Home because the tour spans every top-level
 * section: the screen that owns it has to be the one that can navigate between
 * them.
 */
@HiltViewModel
class TourViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
) : ViewModel() {

    /** False until the stored preference is read, so the scrim never flashes over a boot. */
    val tourVisible = userPreferences.tourCompleted.map { completed -> !completed }

    fun finish() {
        viewModelScope.launch { userPreferences.setTourCompleted() }
    }
}
