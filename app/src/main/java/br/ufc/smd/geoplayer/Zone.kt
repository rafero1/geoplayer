package br.ufc.smd.geoplayer

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

class Zone : Serializable {
    var id: Int = 0
    var lat: Double = 0.0
    var lon: Double = 0.0
    var radius: Double = 200.0
    var playlist: PlaylistActivity
    var fenceId: String = ""

    init {
        this.id = staticId
        staticId += 1
        this.fenceId = "geofence-" + this.id
    }

    private companion object {
        var staticId = 0
    }

    constructor(latitude: Double, longitude: Double, playlist: PlaylistActivity) {
        this.lat = latitude
        this.lon = longitude
        this.playlist = playlist
    }

    constructor(latLng: LatLng) {
        lat = latLng.latitude
        lon = latLng.longitude
        playlist = PlaylistActivity()
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