package br.ufc.smd.geoplayer

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

class Zone : Serializable {
    val id: Int
    var lat: Double = 0.0
    var lon: Double = 0.0
    var radius: Double = 200.0
    var playlist: Playlist

    init {
        id = staticId++
    }

    private companion object {
        var staticId = 0
    }

    constructor(latitude: Double, longitude: Double, playlist: Playlist) {
        this.lat = latitude
        this.lon = longitude
        this.playlist = playlist
    }

    constructor(latLng: LatLng) {
        lat = latLng.latitude
        lon = latLng.longitude
        playlist = Playlist()
    }

    /**
     * Sets [lat] and [lon] to [latLng]
     */
    fun latLng(latLng: LatLng) {
        lat = latLng.latitude
        lon = latLng.longitude
    }

    /**
     * Returns [lat] and [lon] as [LatLng]
     */
    fun latLng(): LatLng = LatLng(lat, lon)
}