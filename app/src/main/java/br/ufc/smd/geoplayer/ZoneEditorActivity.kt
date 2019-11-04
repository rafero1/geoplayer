package br.ufc.smd.geoplayer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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

class ZoneEditorActivity : AppCompatActivity() {

    lateinit var model: ZoneEditorViewModel

    var zone: Zone = Zone(LatLng(0.0, 0.0))

    var selectedSongs: ArrayList<Song> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zone_editor)

        model = ViewModelProviders.of(this)[ZoneEditorViewModel::class.java]

        setTitle("Criar nova Zona")

        val local_extra = intent.getDoubleArrayExtra(MapsActivity.LOCATION_EXTRA)
        val location = LatLng(local_extra[0], local_extra[1])
        edit_zone_tv_location.text = location.toString()

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

            zone.latLng(location)
            zone.radius = edit_zone_tv_radius.text.toString().toDouble()
            zone.playlist = Playlist(edit_zone_tv_name.text.toString(), songList)

            //if (zones.contains(zone)) {
            //    storage.updateZone(zones.indexOf(zone), zone)
            //} else  {
                zones.add(zone)
                storage.storeZones(zones)
            //}

            val gson = Gson()
            val type = object : TypeToken<Zone>() {}.type
            val newZone = gson.toJson(zone, type)
            val i = Intent()
            i.putExtra("br.ufc.smd.geoplayer.newZone", newZone)
            setResult(Activity.RESULT_OK, i)
            finish()
        }
    }
}
