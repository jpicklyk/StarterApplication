package com.example.starterapplication.knox_standard.license.domain.usecase

import android.content.Context
import com.example.starterapplication.knox_standard.BuildConfig
import com.example.starterapplication.knox_standard.license.data.KnoxErrorMapper
import com.example.starterapplication.knox_standard.license.presentation.LicenseState
import com.samsung.android.knox.license.KnoxEnterpriseLicenseManager

import com.samsung.android.knox.license.LicenseResultCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Single
import javax.inject.Inject
import kotlin.coroutines.resume

@Single
internal class KnoxLicenseUseCaseImpl @Inject constructor(
    private val context: Context,
    private val knoxErrorMapper: KnoxErrorMapper
) : KnoxLicenseUseCase {
    override suspend operator fun invoke(activate: Boolean): LicenseState = suspendCancellableCoroutine { continuation ->
        val callback = LicenseResultCallback { licenseResult ->
            val state = when {
                licenseResult == null -> LicenseState.Error("License result is null")
                licenseResult.isSuccess ->
                    if (activate) LicenseState.Activated("License activated successfully")
                    else LicenseState.NotActivated
                else -> {
                    val errorMessage = knoxErrorMapper.getKpeErrorMessage(licenseResult.errorCode)
                    LicenseState.Error("$errorMessage. Details: ${licenseResult.errorCode}")
                }
            }
            continuation.resume(state)
        }

        try {
            val knoxManager = getKnoxManager()
            if (activate) {
                knoxManager.activateLicense(BuildConfig.KNOX_LICENSE_KEY, callback)
            } else {
                knoxManager.deActivateLicense(BuildConfig.KNOX_LICENSE_KEY, callback)
            }
        } catch (e: Exception) {
            val errorCode = when (e) {
                is SecurityException -> KnoxEnterpriseLicenseManager.ERROR_INTERNAL
                is IllegalArgumentException -> KnoxEnterpriseLicenseManager.ERROR_INVALID_LICENSE
                else -> KnoxEnterpriseLicenseManager.ERROR_UNKNOWN
            }
            continuation.resume(LicenseState.Error(knoxErrorMapper.getKpeErrorMessage(errorCode)))
        }
    }

    private fun getKnoxManager(): KnoxEnterpriseLicenseManager {
        return KnoxEnterpriseLicenseManager.getInstance(context)
    }
}