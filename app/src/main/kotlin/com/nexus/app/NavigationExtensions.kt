package com.nexus.app

import androidx.navigation.NavController
import androidx.navigation.NavHostController

fun NavHostController.navigateSafe(destination: NexusDestination) {
    try {
        navigate(destination)
    } catch (_: Exception) {
        // Log navigation error
    }
}
