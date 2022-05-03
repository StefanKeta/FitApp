package com.example.licenta.fragment.main

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.licenta.R
import com.example.licenta.api.APIHandler
import com.example.licenta.api.PlaceAPIService
import com.example.licenta.api.model.GymPlaces
import com.example.licenta.util.PermissionsChecker
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode

class MapsFragment : Fragment(), PlaceSelectionListener, GoogleMap.OnCameraIdleListener,
    View.OnClickListener {

    private lateinit var locationRequestLauncher: ActivityResultLauncher<Array<String>?>
    private lateinit var mapView: View
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var apiService: PlaceAPIService
    private lateinit var map: GoogleMap
    private lateinit var autocompleteSupportFragment: AutocompleteSupportFragment
    private lateinit var searchAreaBtn: Button
    private lateinit var movedCameraLocation: LatLng
    private var currentGyms: GymPlaces? = null
    private var locationMarker: Marker? = null
    private var locationGranted = false
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var lastKnownLocation: Location? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.fragment_maps_map) as SupportMapFragment
        autocompleteSupportFragment =
            childFragmentManager.findFragmentById(R.id.fragment_maps_autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteSupportFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )
        searchAreaBtn = view.findViewById(R.id.fragment_maps_search_area)
        searchAreaBtn.setOnClickListener(this)
        autocompleteSupportFragment.setOnPlaceSelectedListener(this)
        apiService = APIHandler.googlePlacesService
        mapView = mapFragment.requireView()
        locationRequestLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { map ->
            for (entry in map.entries) {
                if (entry.key == Manifest.permission.ACCESS_FINE_LOCATION || entry.key == Manifest.permission.ACCESS_COARSE_LOCATION) setUpMap()
                else requestLocationPermission()
            }
        }
        mapFragment.getMapAsync(::onMapReadyCallback)
        return view
    }

    private fun onMapReadyCallback(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMapClickListener {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, DEFAULT_ZOOM.toFloat()))
        }
        map.setOnCameraIdleListener(this)
        requestLocationPermission()
        moveLocationButtonDown()
    }

    override fun onError(error: Status) {
        Toast.makeText(requireContext(), "Failed with $error", Toast.LENGTH_SHORT).show()
    }

    override fun onPlaceSelected(place: Place) {
        val latLng = place.latLng!!
        map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(place.name)
        )
        getPlaces(latLng, place.name)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM.toFloat()))
    }

    override fun onClick(v: View?) {
        if (v!!.id == R.id.fragment_maps_search_area) {
            getPlaces(movedCameraLocation, null)
            searchAreaBtn.visibility = View.GONE
        }
    }

    override fun onCameraIdle() {
        if (locationGranted) {
            if (lastKnownLocation != null) {
                val phoneLocation = LatLng(
                    BigDecimal(lastKnownLocation!!.latitude).setScale(4, RoundingMode.HALF_EVEN)
                        .toDouble(),
                    BigDecimal(lastKnownLocation!!.longitude).setScale(4, RoundingMode.HALF_EVEN)
                        .toDouble()
                )
                val centeredLocation = LatLng(
                    BigDecimal(map.cameraPosition.target.latitude).setScale(
                        4,
                        RoundingMode.HALF_EVEN
                    ).toDouble(),
                    BigDecimal(map.cameraPosition.target.longitude).setScale(
                        4,
                        RoundingMode.HALF_EVEN
                    )
                        .toDouble()
                )
                if (centeredLocation != phoneLocation) {
                    movedCameraLocation = centeredLocation
                    searchAreaBtn.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun setUpMap() {
        locationGranted = true
        setUpClient()
        updateUserLocation()
        getDeviceLocation()
    }

    private fun setUpClient() {
        Places.initialize(requireContext(), getString(R.string.google_maps_key))
        placesClient = Places.createClient(requireContext())
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun getPlaces(userLocation: LatLng, locationName: String?) {
        map.clear()
        if (locationName != null) {
            map.addMarker(
                MarkerOptions()
                    .position(userLocation)
                    .title(locationName)
            )
        }
        val url =
            "${APIHandler.GOOGLE_MAPS_API_BASE_URL}json?location=${userLocation.latitude},${userLocation.longitude}&radius=$SEARCH_RADIUS&types=gym&sensor=true&key=${
                resources.getString(
                    R.string.google_maps_key
                )
            }"

        apiService.getNearbyGyms(url)
            .enqueue(object : Callback<GymPlaces> {
                override fun onResponse(call: Call<GymPlaces>, response: Response<GymPlaces>) {
                    if (response.isSuccessful) {
                        currentGyms = response.body()
                        for (i in 0 until (currentGyms?.results?.size ?: 0)) {
                            val place = currentGyms!!.results[i]
                            val placeLat = place.geometry!!.location.lat
                            val placeLong = place.geometry.location.lng
                            val latLng = LatLng(placeLat!!, placeLong!!)
                            val name = place.name
                            val markerOptions = MarkerOptions()
                                .position(latLng)
                                .title(name)
                                .icon(
                                    BitmapDescriptorFactory
                                        .fromResource(R.drawable.dumbbell)
                                )
                            map.addMarker(markerOptions)
                        }
                    }
                }

                override fun onFailure(call: Call<GymPlaces>, t: Throwable) {
                    Toast.makeText(context!!, "${t.printStackTrace()}", Toast.LENGTH_SHORT).show()
                }

            })
    }


    @SuppressLint("MissingPermission")
    private fun updateUserLocation() {
        try {
            if (locationGranted) {
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
            } else {
                map.isMyLocationEnabled = false
                map.uiSettings.isMyLocationButtonEnabled = false
                requestLocationPermission()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (locationGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            val location =
                                LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                            searchAreaBtn.visibility = View.GONE
                            getPlaces(location, "Device location")
                        }
                    } else {
                        map.clear()
                        locationMarker = map.addMarker(
                            MarkerOptions().position(defaultLocation).title("Marker in Sydney")
                        )
                        map.animateCamera(CameraUpdateFactory.newLatLng(defaultLocation))
                        map.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun requestLocationPermission() {
        locationRequestLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    private fun moveLocationButtonDown() {
        if (mapView.findViewById<View>(Integer.parseInt("1")) != null) {
            val locationButton = (mapView.findViewById<View>("1".toInt())
                .parent as View).findViewById<View>("2".toInt())

            // and next place it, on bottom right (as Google Maps app)
            val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 150);
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 14
        private const val SEARCH_RADIUS = 1500
    }

}