package br.ufc.smd.geoplayer

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class MapsViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var zones: ArrayList<Zone>

    init {
        loadZones()
    }

    fun loadZones() {
        zones = StorageUtil(getApplication()).loadZones()
    }

    fun addZone(zone: Zone) {
        zones.add(zone)
        StorageUtil(getApplication()).storeZones(zones)
    }
}