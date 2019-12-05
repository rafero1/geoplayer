package br.ufc.smd.geoplayer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.awareness.fence.HeadphoneFence
import com.google.android.gms.awareness.fence.LocationFence
import com.google.android.gms.awareness.state.HeadphoneState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson

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
        set(value) {
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

            // Acquire a reference to the system Location Manager
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationProvider: String = LocationManager.NETWORK_PROVIDER
            val lastKnownLocation: Location? = locationManager.getLastKnownLocation(locationProvider)

            if (lastKnownLocation != null) {
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            lastKnownLocation.latitude,
                            lastKnownLocation.longitude
                        ), 15f
                    )
                )
                // Define a listener that responds to location updates
                val locationListener = object : LocationListener {

                    override fun onLocationChanged(location: Location) {
                        // Called when a new location is found by the network location provider.
                        //if (location != null)
                        //text.setText(lastKnownLocation.toString())

                    }

                    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                    }

                    override fun onProviderEnabled(provider: String) {
                    }

                    override fun onProviderDisabled(provider: String) {
                    }
                }
            }

           // Register the listener with the Location Manager to receive location updates
           locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)

            mMap.setOnMapClickListener {
                Toast.makeText(this, "Segure para criar uma zona", Toast.LENGTH_SHORT).show()
            }

            mMap.setOnMapLongClickListener { position ->
                val newZone = Zone(position)
                createNewZoneActivity(newZone)
            }

            //Desliga rotação via gesto
            mMap.uiSettings.setRotateGesturesEnabled(false)

            //Adiciona o botão de minha localizaçãot
            mMap.setMyLocationEnabled(true);

            //Adiciona o listener do ponto azul no mapa
            mMap.setOnMyLocationClickListener{position ->
                val newZone = Zone(LatLng(position.latitude, position.longitude))
                createNewZoneActivity(newZone)
            }

            //Seta o callback do botão de minha localização (false é pra ele centralizar no usuário)
            mMap.setOnMyLocationButtonClickListener{
                false
            }

            displayZones()
        }
    }

    private fun displayZones () {
        model.zones.forEachIndexed { index, zone ->
            addZoneToMap(index, zone)
            Log.d("DEBUG", "[${zone.id}] " + zone.playlist.name + "(${zone.fenceId})" + " " + zone.latLng().toString())
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

    /*private fun startSnapshot(): Location {
        //val myLocation: Location
        var snapshotClient = Awareness.getSnapshotClient(this)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED)
        {

            snapshotClient.location
                .addOnSuccessListener(object : OnSuccessListener<LocationResponse> {
                    override fun onSuccess(locationResponse: LocationResponse) {
                        Log.v("test", locationResponse.location.toString())
                        if (locationResponse.location != null) {
                            //Eu quero a localização aqui, mas tá crashando
                            //myLocation?.latitude = locationResponse.location.latitude
                            //myLocation?.longitude = locationResponse.location.longitude
                            myLocation = locationResponse.location

                        }

                        //Log.v("test2", myLocation?.toString())
                    }
                })

            /*.addOnFailureListener(object : OnFailureListener
            {
                override fun onFailure(ex: Exception)
                {
                    Log.e("test", "onFailure(): " + ex)
                    return
                }
            })*/
        }*/


        private val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                //thetext.setText("" + location.longitude + ":" + location.latitude);
            }
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
}
