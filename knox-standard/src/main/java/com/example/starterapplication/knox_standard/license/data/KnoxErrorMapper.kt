package com.example.starterapplication.knox_standard.license.data

import com.example.starterapplication.core.common.presentation.ResourceProvider
import com.example.starterapplication.knox_standard.R
import com.samsung.android.knox.license.KnoxEnterpriseLicenseManager
import org.koin.core.annotation.Single

@Single
class KnoxErrorMapper (
    private val resourceProvider: ResourceProvider
) {
    fun getKpeErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            KnoxEnterpriseLicenseManager.ERROR_INTERNAL ->
                resourceProvider.getString(R.string.knox_standard_err_kpe_internal)
            KnoxEnterpriseLicenseManager.ERROR_INTERNAL_SERVER ->
                resourceProvider.getString(R.string.knox_standard_err_kpe_internal_server)
            KnoxEnterpriseLicenseManager.ERROR_INVALID_LICENSE ->
                resourceProvider.getString(R.string.knox_standard_err_kpe_licence_invalid_license)
            KnoxEnterpriseLicenseManager.ERROR_INVALID_PACKAGE_NAME ->
                resourceProvider.getString(R.string.knox_standard_err_kpe_invalid_package_name)
            KnoxEnterpriseLicenseManager.ERROR_LICENSE_TERMINATED ->
                resourceProvider.getString(R.string.knox_standard_err_kpe_licence_terminated)
            KnoxEnterpriseLicenseManager.ERROR_NETWORK_DISCONNECTED ->
                resourceProvider.getString(R.string.knox_standard_err_kpe_network_disconnected)
            KnoxEnterpriseLicenseManager.ERROR_NETWORK_GENERAL ->
                resourceProvider.getString(R.string.knox_standard_err_kpe_network_general)
            KnoxEnterpriseLicenseManager.ERROR_NOT_CURRENT_DATE ->
                resourceProvider.getString(R.string.knox_standard_err_kpe_not_current_date)
            KnoxEnterpriseLicenseManager.ERROR_NULL_PARAMS ->
                resourceProvider.getString(R.string.knox_standard_err_kpe_null_params)
            KnoxEnterpriseLicenseManager.ERROR_UNKNOWN ->
                resourceProvider.getString(R.string.knox_standard_err_kpe_unknown)
            KnoxEnterpriseLicenseManager.ERROR_USER_DISAGREES_LICENSE_AGREEMENT ->
                resourceProvider.getString(
                    R.string.knox_standard_err_kpe_user_disagrees_license_agreement
                )
            KnoxEnterpriseLicenseManager.ERROR_LICENSE_DEACTIVATED ->
                resourceProvider.getString(R.string.knox_standard_err_kpe_license_deactivated)
            KnoxEnterpriseLicenseManager.ERROR_LICENSE_EXPIRED ->
                resourceProvider.getString(R.string.knox_standard_err_kpe_license_expired)
            KnoxEnterpriseLicenseManager.ERROR_LICENSE_QUANTITY_EXHAUSTED ->
                resourceProvider.getString(
                    R.string.knox_standard_err_kpe_license_quantity_exhausted
                )
            KnoxEnterpriseLicenseManager.ERROR_LICENSE_ACTIVATION_NOT_FOUND ->
                resourceProvider.getString(
                    R.string.knox_standard_err_kpe_license_activation_not_found
                )
            KnoxEnterpriseLicenseManager.ERROR_LICENSE_QUANTITY_EXHAUSTED_ON_AUTO_RELEASE ->
                resourceProvider.getString(
                    R.string.knox_standard_err_kpe_license_quantity_exhausted_on_auto_release
                )
            else -> resourceProvider.getString(
                R.string.knox_standard_err_kpe_code_unknown,
                errorCode.toString()
            )
        }
    }
}