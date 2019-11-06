package br.ufc.smd.geoplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.awareness.fence.FenceState


class ZoneFenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val fenceState = FenceState.extract(intent)

        if (TextUtils.equals(fenceState.fenceKey, "geofence")) {
            val fenceStateStr: String
            when (fenceState.currentState) {
                FenceState.TRUE -> fenceStateStr = "true"
                FenceState.FALSE -> fenceStateStr = "false"
                FenceState.UNKNOWN -> fenceStateStr = "unknown"
                else -> fenceStateStr = "unknown value"
            }
            Toast.makeText(context, "Fence: ${fenceStateStr}", Toast.LENGTH_SHORT).show()
        }

    }
}