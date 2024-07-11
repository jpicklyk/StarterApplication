package com.example.starterapplication.knox_standard.license.domain.usecase

import com.example.starterapplication.knox_standard.license.presentation.LicenseState

interface KnoxLicenseUseCase {
    suspend operator fun invoke(activate: Boolean = true): LicenseState
}