package com.example.starterapplication.receivers

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class AdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        /* If you wanted to use the DevicePolicyManager and ComponentName, here is how you get them.
            val manager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val component = ComponentName(context.applicationContext, AdminReceiver::class.java)
        */

        /* If you wanted to enable lock task mode, it is done here
         * To each activity in your application manifest add: android:lockTaskMode="if_whitelisted"
         * and the activity will be automatically launched in lock task mode.
         */
        //manager.setLockTaskPackages(component, arrayOf(context.packageName))

    }

}