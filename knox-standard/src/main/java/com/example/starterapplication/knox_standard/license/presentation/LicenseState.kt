package com.example.starterapplication.knox_standard.license.presentation

sealed class LicenseState {
    data object Loading : LicenseState()
    data object NotActivated : LicenseState()
    data class Activated(val message: String) : LicenseState()
    data object Expired : LicenseState()
    data object Terminated : LicenseState()
    data class Error(val message: String) : LicenseState()
}