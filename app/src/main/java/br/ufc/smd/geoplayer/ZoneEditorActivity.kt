package br.ufc.smd.geoplayer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_zone_editor.*
import java.text.DecimalFormat

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

        //var lat:String = "%.4f".format(zone.latLng().latitude.toString())
        //var long:String = "%.4f".format(zone.latLng().longitude.toString())
        val lat = zone.latLng().latitude
        val long = zone.latLng().longitude

        val df = DecimalFormat("#.####")

        //edit_zone_tv_location.text = zone.latLng().toString()
        edit_zone_tv_location.text = "Lat: " + df.format(lat) + ", Long: " + df.format(long)
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

        edit_zone_btn_ok.setOnClickListener { view ->
            val zones = storage.loadZones()

            val songList: ArrayList<Song> = (recyclerView.adapter as EditSongListAdapter).checked

            val updatedZone = zone
            updatedZone.radius = edit_zone_tv_radius.text.toString().toDouble()
            updatedZone.playlist = PlaylistActivity(edit_zone_tv_name.text.toString(), songList)

            if (zones.isEmpty()) {
                zones.add(updatedZone)
                storage.storeZones(zones)
            } else {
                var updated = false
                zones.forEachIndexed { index, currentZone ->
                    if (currentZone.id == zone.id) {
                        storage.updateZone(index, updatedZone)
                        Log.d("DEBUG", "updoot")
                        updated = true
                    } else if (!updated && index == zones.lastIndex) {
                        zones.add(updatedZone)
                        storage.storeZones(zones)
                        Log.d("DEBUG", "new")
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
