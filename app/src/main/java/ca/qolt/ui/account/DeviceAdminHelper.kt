package ca.qolt.ui.account

import android.content.Context
import android.content.Intent
import android.provider.Settings


object DeviceAdminHelper {

    fun openDeviceAdminSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
            context.startActivity(fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
