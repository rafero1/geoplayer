package br.ufc.smd.geoplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.android.gms.awareness.fence.FenceState


class ZoneFenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val fenceState = FenceState.extract(intent)

        var fences = setOf<String>()

        if (context != null)
            fences = StorageUtil(context).loadFences()

        fences.forEach { fenceName ->
            if (TextUtils.equals(fenceState.fenceKey, fenceName)) {
                val fenceStateStr: String
                when (fenceState.currentState) {
                    FenceState.TRUE -> fenceStateStr = "true"
                    FenceState.FALSE -> fenceStateStr = "false"
                    FenceState.UNKNOWN -> fenceStateStr = "unknown"
                    else -> fenceStateStr = "unknown value"
                }
                Log.d("DEBUG", "fence $fenceName state : $fenceStateStr")
                Toast.makeText(context, "Fence $fenceName : $fenceStateStr", Toast.LENGTH_SHORT).show()

                if (fenceStateStr == "true") {
                    if (context != null) {
                        val nextSongs = StorageUtil(context).loadZone(fenceName)?.playlist?.songs
                        if (nextSongs != null)
                            StorageUtil(context).storeEnqueuedSongs(nextSongs)
                    }
                }
            }
        }
    }
}