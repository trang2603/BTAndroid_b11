package com.demo
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.demo.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapsActivity :
    AppCompatActivity(),
    OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment =
            supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val secondLocation = LatLng(21.014007826514074, 105.78438394043823)
        val secondMarker = mMap.addMarker(MarkerOptions().position(secondLocation).title("Marker in second location"))
        secondMarker?.showInfoWindow()

        mMap.uiSettings.isZoomControlsEnabled = true

        enableMyLocation()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE,
            )
        } else {
            // Permission has already been granted
            mMap.isMyLocationEnabled = true
            getDeviceLocation()
        }
    }

    private fun getDeviceLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val lastKnownLocation: Location? = task.result
                        if (lastKnownLocation != null) {
                            val currentLatLng = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                            mMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))

                            val secondLocation = LatLng(21.014007826514074, 105.78438394043823)
                            mMap.addMarker(MarkerOptions().position(secondLocation).title("Marker in second location"))

                            val builder = LatLngBounds.Builder()
                            builder.include(currentLatLng)
                            builder.include(secondLocation)
                            val bounds = builder.build()

                            val padding = 100 // offset from edges of the map in pixels
                            val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)

                            mMap.animateCamera(cu)

                            val polyline =
                                mMap.addPolyline(
                                    PolylineOptions()
                                        .add(currentLatLng, secondLocation)
                                        .color(ContextCompat.getColor(this, R.color.gray)),
                                )

                            val distance =
                                calculateDistance(
                                    lastKnownLocation.latitude,
                                    lastKnownLocation.longitude,
                                    secondLocation.latitude,
                                    secondLocation.longitude,
                                )

                            mMap.setOnPolylineClickListener {
                                Toast.makeText(this, "Khoảng cách: $distance mét", Toast.LENGTH_LONG).show()
                            }
                            polyline.isClickable = true // Enable polyline click
                        }
                    } else {
                        // Handle the case where the location is null
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Float {
        val loc1 =
            Location("").apply {
                latitude = lat1
                longitude = lon1
            }
        val loc2 =
            Location("").apply {
                latitude = lat2
                longitude = lon2
            }
        return loc1.distanceTo(loc2)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
// Permission was granted, enable the my-location layer
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    mMap.isMyLocationEnabled = true
                    getDeviceLocation()
                }
            } else {
                // Permission denied, show a message to the user
            }
        }
    }
}
