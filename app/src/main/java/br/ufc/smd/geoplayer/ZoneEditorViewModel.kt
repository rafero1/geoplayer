package br.ufc.smd.geoplayer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.maps.model.LatLng

class ZoneEditorViewModel(application: Application) : AndroidViewModel(application) {

    var zone: Zone = Zone(LatLng(0.0, 0.0))
    var selectedSongs: ArrayList<Song> = arrayListOf()

    fun loadZone() {

    }
}