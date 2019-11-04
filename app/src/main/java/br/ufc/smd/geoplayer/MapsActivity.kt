package br.ufc.smd.geoplayer

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var model: MapsViewModel

    private lateinit var mMap: GoogleMap

    companion object {
        const val LOCATION_EXTRA = "br.ufc.smd.geoplayer.location"
        const val CREATE_ZONE = 0

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        model = ViewModelProviders.of(this)[MapsViewModel::class.java]

        model.loadZones()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-3.706558, -38.563641), 15f))

        mMap.setOnMapClickListener {
            Toast.makeText(this, "Segure para criar uma zona", Toast.LENGTH_SHORT).show()
        }

        mMap.setOnMapLongClickListener { position ->
            val newZone = Zone(position)
            createNewZoneActivity(newZone)
        }

        model.zones.forEach { zone ->
            addZoneToMap(zone)
            Log.d("MAPS", zone.playlist.name + " " + zone.latLng().toString() + " - " + zone.playlist.songs.toString())
        }
    }

    private fun addZoneToMap(zone: Zone) {
        mMap.apply {
            addMarker(MarkerOptions()
                .position(zone.latLng())
                .title(zone.playlist.name)
            )
            addCircle(CircleOptions()
                .center(zone.latLng())
                .radius(zone.radius)
                .fillColor(Color.argb(180, 200, 115, 115))
                .strokeColor(Color.rgb(200, 115, 115))
            )
        }
    }

    private fun createNewZoneActivity(newZone: Zone) {
        val i = Intent(this, ZoneEditorActivity::class.java)
        i.putExtra(LOCATION_EXTRA, doubleArrayOf(newZone.lat, newZone.lon))
        startActivityForResult(i, CREATE_ZONE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CREATE_ZONE) {
            if (resultCode == Activity.RESULT_OK) {
                val gson = Gson()
                val type = object : TypeToken<Zone>() {}.type
                val newZone = gson.fromJson<Zone>(data!!.getStringExtra("br.ufc.smd.geoplayer.newZone")!!, type)
                addZoneToMap(newZone)
                Toast.makeText(this, newZone.playlist.name + " criada com sucesso!", Toast.LENGTH_SHORT).show()
                Log.d("MAPS", "added: " + newZone.playlist.name + " " + newZone.latLng().toString() + " - " + newZone.playlist.songs.toString())
            }
        }
    }

}
