package br.ufc.smd.geoplayer

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.FenceClient;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.HeadphoneFence;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import br.ufc.smd.geoplayer.NotificationAction;
import com.google.android.gms.awareness.SnapshotClient
import com.google.android.gms.awareness.snapshot.LocationResponse

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var model: MapsViewModel

    private lateinit var mMap: GoogleMap

    companion object {
        const val LOCATION_EXTRA = "br.ufc.smd.geoplayer.location"
        const val CREATE_ZONE = 0

    }

    //Deve ter algo errado nessa declaração
    var myLocation: Location
        get() = myLocation
        set(value:Location) {
            this.myLocation.latitude = value.latitude
            this.myLocation.longitude = value.longitude
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED)
        {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

            model = ViewModelProviders.of(this)[MapsViewModel::class.java]

            model.loadZones()

            val headphone = HeadphoneFence.during(HeadphoneState.PLUGGED_IN)

            Awareness.getSnapshotClient(this).location
                .addOnSuccessListener(object : OnSuccessListener<LocationResponse> {
                    override fun onSuccess(locationResponse: LocationResponse) {
                        Log.v("test", locationResponse.location.toString())

                        //Eu quero a localização aqui, mas tá crashando
                        //myLocation.set(locationResponse.location)

                    }
                })
                .addOnFailureListener(object : OnFailureListener {
                    override fun onFailure(ex: Exception) {
                        Log.e("test", "onFailure(): " + ex)
                    }
                })
        }
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
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap = googleMap

            //DEPRECADO
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-3.706558, -38.563641), 15f))
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(Awareness.getSnapshotClient(this).location.))

            //TEM ALGO ERRADO NO MYLOCATION, mas é meio que isso aqui
            //val mylocation = Awareness.getSnapshotClient(this).location

            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(myLocation.latitude, myLocation.longitude), 15f))

            mMap.setOnMapClickListener {
                Toast.makeText(this, "Segure para criar uma zona", Toast.LENGTH_SHORT).show()
            }

            mMap.setOnMapLongClickListener { position ->
                val newZone = Zone(position)
                createNewZoneActivity(newZone)
            }

            //Desliga rotação via gesto
            mMap.uiSettings.setRotateGesturesEnabled(false)

            //Adiciona o botão de minha localização
            mMap.setMyLocationEnabled(true);

            //Adiciona o listener do ponto azul no mapa
            mMap.setOnMyLocationClickListener{
                true
            }
            //mMap.setOnMyLocationClickListener{
              //  true
            //}

            //Seta o callback do botão de minha localização (false é pra ele centralizar no usuário)
            mMap.setOnMyLocationButtonClickListener{
                false
            }

            //mMap.setonmy

            displayZones()
        }
    }

    private fun displayZones () {
        model.zones.forEachIndexed { index, zone ->
            addZoneToMap(index, zone)
            Log.d("MAPS", zone.playlist.name + " " + zone.latLng().toString() + " - " + zone.playlist.songs.toString())
        }
    }

    private fun addZoneToMap(index: Int, zone: Zone) {
        mMap.apply {
            addMarker(MarkerOptions()
                .position(zone.latLng())
                .title(zone.playlist.name)
            ).tag = index
            addCircle(CircleOptions()
                .center(zone.latLng())
                .radius(zone.radius)
                .fillColor(Color.argb(180, 200, 115, 115))
                .strokeColor(Color.rgb(200, 115, 115))
            )
            setOnInfoWindowClickListener { marker ->
                createNewZoneActivity(model.zones[marker.tag as Int])
            }
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            val location = LocationFence.entering(zone.lat, zone.lon, zone.radius)
        }
    }

    private fun createNewZoneActivity(newZone: Zone) {
        val i = Intent(this, ZoneEditorActivity::class.java)
        val gson = Gson()
        val json = gson.toJson(newZone)
        i.apply {
            //putExtra(LOCATION_EXTRA, doubleArrayOf(newZone.lat, newZone.lon))
            putExtra(LOCATION_EXTRA, json)
        }

        startActivityForResult(i, CREATE_ZONE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CREATE_ZONE) {
            if (resultCode == Activity.RESULT_OK) {
                mMap.clear()
                model.loadZones()
                displayZones()
            }
        }
    }


    fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
        Log.v("test", "MyLocation")
    }


    fun onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        Log.v("test", "MyLocationButton")
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        false
    }


}
