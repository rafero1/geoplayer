package br.ufc.smd.geoplayer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_zone_editor.*

class ZoneEditorActivity : AppCompatActivity() {

    lateinit var model: ZoneEditorViewModel

    var zone: Zone = Zone(LatLng(0.0, 0.0))

    var selectedSongs: ArrayList<Song> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zone_editor)

        model = ViewModelProviders.of(this)[ZoneEditorViewModel::class.java]

        setTitle("Criar nova Zona")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val gson = Gson()
        val json = intent.getStringExtra(MapsActivity.LOCATION_EXTRA)
        val type = object : TypeToken<Zone>() {}.type
        zone = gson.fromJson(json, type)

        edit_zone_tv_location.text = zone.latLng().toString()
        edit_zone_tv_name.setText(zone.playlist.name)
        edit_zone_tv_radius.setText(zone.radius.toString())

        val storage = StorageUtil(this)
        val allSongs = storage.loadSongs()

        selectedSongs = zone.playlist.songs

        val recyclerView = findViewById<RecyclerView>(R.id.edit_zone_rv_songs)
        recyclerView.apply {
            adapter = EditSongListAdapter(allSongs, selectedSongs, application)
            layoutManager = LinearLayoutManager(this@ZoneEditorActivity)
            addItemDecoration(
                DividerItemDecoration(
                    recyclerView.context,
                    LinearLayoutManager.VERTICAL
                )
            )
            addOnItemTouchListener(CustomTouchListener(this@ZoneEditorActivity, object : ItemTouchListener {
                override fun onClick(view: View, index: Int) {
                    if (selectedSongs.contains(allSongs[index]))
                        selectedSongs.remove(allSongs[index])
                    else
                        selectedSongs.add(allSongs[index])
                    recyclerView.adapter?.notifyItemChanged(index)
                }
            }))
        }

        /*if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // Acquire a reference to the system Location Manager
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationProvider: String = LocationManager.NETWORK_PROVIDER
            val lastKnownLocation: Location = locationManager.getLastKnownLocation(locationProvider)
            // Define a listener that responds to location updates
            val locationListener = object : LocationListener {

                override fun onLocationChanged(location: Location) {
                    // Called when a new location is found by the network location provider.
                    if (location != null)
                        text.setText(lastKnownLocation.toString())

                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                }

                override fun onProviderEnabled(provider: String) {
                }

                override fun onProviderDisabled(provider: String) {
                }
            }

            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
        }*/

        edit_zone_btn_ok.setOnClickListener { view ->
            val zones = storage.loadZones()

            val songList: ArrayList<Song> = (recyclerView.adapter as EditSongListAdapter).checked

            val updatedZone = zone
            updatedZone.radius = edit_zone_tv_radius.text.toString().toDouble()
            updatedZone.playlist = Playlist(edit_zone_tv_name.text.toString(), songList)

            if (zones.isEmpty()) {
                zones.add(updatedZone)
                storage.storeZones(zones)
            } else {
                zones.forEachIndexed { index, currentZone ->
                    if (currentZone.id == zone.id) {
                        storage.updateZone(index, updatedZone)
                    } else if (currentZone == zones.last()) {
                        zones.add(updatedZone)
                        storage.storeZones(zones)
                    }
                }
            }

            val gson = Gson()
            val type = object : TypeToken<Zone>() {}.type
            val newZone = gson.toJson(updatedZone, type)
            val i = Intent()
            i.putExtra("br.ufc.smd.geoplayer.newZone", newZone)
            setResult(Activity.RESULT_OK, i)
            finish()
        }
    }
}
