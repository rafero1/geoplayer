package br.ufc.smd.geoplayer


import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.VibrationEffect
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.google.android.gms.awareness.fence.FenceState

class NotificationAction : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val state = FenceState.extract(intent).currentState
        if (state == FenceState.TRUE)
            pushNotification(context)
    }

    fun pushNotification(context: Context) {
        val builder = NotificationCompat.Builder(context, "channelId")
        builder.setContentTitle("GeoPlayer")
        builder.setTicker("Mudanca de contexto detectada")
        builder.setContentText("Você está andando com headphones!")

        val not = builder.build()
        val manager = NotificationManagerCompat.from(context)
        manager.notify(123, not)
    }
}