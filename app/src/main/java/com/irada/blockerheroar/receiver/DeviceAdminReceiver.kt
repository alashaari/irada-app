package com.irada.blockerheroar.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context

class DeviceAdminReceiver : DeviceAdminReceiver() {
    
    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, DeviceAdminReceiver::class.java)
        }
    }
}