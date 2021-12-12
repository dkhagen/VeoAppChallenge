package com.example.veoappchallenge.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.veoappchallenge.R
import com.example.veoappchallenge.databinding.ActivityMapsBinding
import com.example.veoappchallenge.model.Trip
import com.example.veoappchallenge.utils.LocationUtils
import com.example.veoappchallenge.utils.PermissionsUtils
import com.example.veoappchallenge.viewmodel.MapsActivityViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.GeoApiContext

/**
 * This is the primary activity that handles the map interactions. It implements a number of
 * listeners/callbacks in order to track the different inputs. It will also request permissions if
 * they are not granted yet. It handles map movements/zooms as well based on these inputs. A user
 * can start tracking a Trip once they have selected a destination. They can also view saved trips
 * by clicking the floating action button.
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback, OnMyLocationButtonClickListener,
    OnMyLocationClickListener {

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * [.onRequestPermissionsResult].
     */
    private var permissionDenied = false
    private var requestingLocationUpdates = false
    private var currentTrip: Trip? = null
    private var currentLocation: LatLng? = null
    private var tripDestination: LatLng? = null
    private var locationCallback: LocationCallback? = null
    private var lastKnownLocation: Location? = null
    private lateinit var mapsActivityViewModel: MapsActivityViewModel
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var btnStartTrip: Button
    private lateinit var btnEndTrip: Button
    private lateinit var btnViewTrips: FloatingActionButton
    private lateinit var llTripInfo: LinearLayout
    private lateinit var chronoTimer: Chronometer
    private lateinit var tvDistanceTraveled: TextView
    private lateinit var tvDistanceRemaining: TextView
    private lateinit var mGeoApiContext: GeoApiContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            requestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES)
        }
        // Grab the view model for this class
        mapsActivityViewModel = ViewModelProvider(this).get(MapsActivityViewModel::class.java)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bindViews()
        setButtonOnClickListeners()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mGeoApiContext = GeoApiContext().setApiKey(getString(R.string.api_key))
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) {
            startLocationUpdates()
        }
    }

    private fun bindViews() {
        btnStartTrip = findViewById(R.id.btn_start_trip)
        btnEndTrip = findViewById(R.id.btn_end_trip)
        btnViewTrips = findViewById(R.id.btn_view_trips)
        llTripInfo = findViewById(R.id.ll_trip_info)
        chronoTimer = findViewById(R.id.chrono_timer)
        tvDistanceTraveled = findViewById(R.id.tv_distance_tracker)
        tvDistanceRemaining = findViewById(R.id.tv_remaining_distance)
    }

    private fun setButtonOnClickListeners() {
        btnStartTrip.setOnClickListener(getDisabledOnClickListener())
        btnEndTrip.setOnClickListener(getEndOnClickListener())
        btnViewTrips.setOnClickListener(getViewTripsOnClickListener())
    }

    private fun startViewTripsActivity() {
        val intent = Intent(this, ViewTripsActivity::class.java)
        startActivity(intent)
    }

    /**
     * When the user clicks start we want to begin tracking their progress on their selected path.
     * We hide the start button and show the trip progress layout instead. We also set the end trip
     * button listener since that button is used in two cases.
     */
    private fun getStartOnClickListener(): View.OnClickListener {
        return View.OnClickListener {
            btnStartTrip.visibility = View.GONE
            btnViewTrips.visibility = View.GONE
            llTripInfo.visibility = View.VISIBLE
            startTimer()
            startMeasuringDistance()
            btnEndTrip.setOnClickListener(getEndOnClickListener())
        }
    }

    // If the user has not created a marker, they can not start the trip yet.
    private fun getDisabledOnClickListener(): View.OnClickListener {
        return View.OnClickListener {
            Toast.makeText(this, getString(R.string.toast_tap_to_select_location), Toast.LENGTH_LONG).show()
        }
    }

    // If the user has ends their trip or they have reached their destination we call endTrip().
    private fun getEndOnClickListener(): View.OnClickListener {
        return View.OnClickListener {
            endTrip()
        }
    }

    private fun getViewTripsOnClickListener(): View.OnClickListener {
        return View.OnClickListener {
            startViewTripsActivity()
        }
    }

    // We stop the chronometer, stop the distance, and set the new listener and text for the button.
    private fun endTrip() {
        stopTimer()
        stopMeasuringDistance()
        btnViewTrips.visibility = View.VISIBLE
        btnEndTrip.text = getString(R.string.reset)
        btnEndTrip.setOnClickListener {
            resetTrip()
        }
        Toast.makeText(this, getString(R.string.user_arrived), Toast.LENGTH_LONG).show()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)
        enableMyLocation()
        getDeviceLocation(true)
        mMap.setMaxZoomPreference(20f)
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18f))
        mMap.setOnMapClickListener(getRouteFromClickListener())
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!::mMap.isInitialized) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            getDeviceLocation(true)
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionsUtils().requestPermission(
                this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
        fusedLocationClient.lastLocation.addOnSuccessListener {
            mMap.moveCamera(
                CameraUpdateFactory.newLatLng(
                    LatLng(it.latitude, it.longitude)
                )
            )
            // only triggering once
            currentLocation = LatLng(it.latitude, it.longitude)
        }
    }

    /**
     * This is run when the user taps somewhere on the map. It generates a marker at that location
     * and calls the Directions API to get a polyline to render onto the map. It also moves the
     * camera to an appropriate zoom level and coordinates based on the bounds returned in the
     * response.
     */
    private fun getRouteFromClickListener(): GoogleMap.OnMapClickListener {
        return GoogleMap.OnMapClickListener { point ->
            mMap.clear()
            val newMarkerLocation = LatLng(point.latitude, point.longitude)
            tripDestination = newMarkerLocation
            lastKnownLocation?.let { location ->
                // Load a new Trip into currentTrip
                currentTrip = Trip(LatLng(location.latitude, location.longitude), tripDestination)
                val responseLiveData = mapsActivityViewModel.getDirectionsResponseData()
                if (!responseLiveData.hasActiveObservers()) {
                    responseLiveData
                        .observe(this,
                            { directionsResponse ->
                                if (directionsResponse != null) {
                                    val routeColor = ContextCompat.getColor(applicationContext,
                                        R.color.route_blue
                                    )
                                    val polyLineOptions = mapsActivityViewModel.createPolyLineOptions(directionsResponse, routeColor)
                                    val padding = (resources.displayMetrics.widthPixels * 0.20).toInt()
                                    val cameraUpdate = mapsActivityViewModel.createCameraUpdate(padding)

                                    mMap.addMarker(MarkerOptions().position(tripDestination!!))
                                    mMap.addPolyline(polyLineOptions)
                                    mMap.animateCamera(cameraUpdate)
                                    tvDistanceRemaining.visibility = View.VISIBLE
                                    tvDistanceRemaining.text =
                                        LocationUtils.updateDistanceText(currentTrip!!.distanceRemaining, false)
                                    enableStartButton()
                                } else {
                                    Toast.makeText(this@MapsActivity, getString(R.string.error_getting_route), Toast.LENGTH_LONG)
                                        .show()
                                }
                            })
                }
                getDeviceLocation(false)
                // Use the view model to call the API
                mapsActivityViewModel.callDirectionsApi(
                    "${location.latitude},${location.longitude}",
                    "${point.latitude},${point.longitude}",
                    getString(R.string.api_key)
                )
            }
        }

    }

    /**
     * Disable the map click listener while a Trip is active. This also sets up the location
     * callback to draw the user's path.
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mMap.setOnMapClickListener(null)
        val pathColor = ContextCompat.getColor(applicationContext, R.color.path_blue)
        currentTrip?.startTime = System.currentTimeMillis()
        locationCallback = LocationUtils.getLocationCallback(currentTrip!!, mMap, pathColor, tvDistanceTraveled, tvDistanceRemaining, ::endTrip)
        fusedLocationClient.requestLocationUpdates(
            LocationUtils.createLocationRequest(),
            locationCallback!!,
            Looper.getMainLooper()
        )
        tvDistanceRemaining.visibility = View.VISIBLE
        requestingLocationUpdates = true
    }

    // Removes the locationCallback if it's not null.
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback ?: object :
            LocationCallback() {})
        requestingLocationUpdates = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (PermissionsUtils().isPermissionGranted(
                permissions,
                grantResults
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            showMissingPermissionError()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mMap.let {
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
            outState.putBoolean(REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
        }
        super.onSaveInstanceState(outState)
    }

    @SuppressLint("MissingPermission")
    override fun onMyLocationButtonClick(): Boolean {
        fusedLocationClient.lastLocation.addOnSuccessListener {
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(it.latitude, it.longitude),
                    15f
                )
            )
            currentLocation = LatLng(it.latitude, it.longitude)
        }
        return false
    }

    // Simply shows the user's location if they tap on their dot.
    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(
            this,
            "Current location:\n${p0.latitude}, ${p0.longitude}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showMissingPermissionError() {
        PermissionsUtils().PermissionDeniedDialog()
            .newInstance(true).show(supportFragmentManager, DIALOG_TAG)
    }

    // Reset the chrono timer
    private fun startTimer() {
        chronoTimer.base = SystemClock.elapsedRealtime()
        chronoTimer.start()
    }

    // Store the trip
    private fun stopTimer() {
        storeTrip()
        chronoTimer.stop()
    }

    private fun startMeasuringDistance() {
        if (!requestingLocationUpdates) {
            startLocationUpdates()
            btnEndTrip.text = getString(R.string.end_trip)
        }
    }

    private fun stopMeasuringDistance() {
        if (requestingLocationUpdates) {
            stopLocationUpdates()
            requestingLocationUpdates = false
        }
    }

    // This fully resets the map and removes any markers, polylines, or trip information.
    private fun resetTrip() {
        currentTrip = null
        mMap.setOnMapClickListener(getRouteFromClickListener())
        mMap.clear()
        getDeviceLocation(true)
        llTripInfo.visibility = View.GONE
        tvDistanceRemaining.visibility = View.GONE
        btnStartTrip.visibility = View.VISIBLE
        chronoTimer.base = SystemClock.elapsedRealtime()
        disableStartButton()
    }

    // We open a dialog fragment asking if the user wants to store this trip.
    private fun storeTrip() {
        currentTrip?.duration = ((System.currentTimeMillis() - currentTrip!!.startTime) / 1000).toInt()
        StoreTripDialogFragment(mapsActivityViewModel.getStoreTripOnClick(currentTrip!!))
            .show(supportFragmentManager, StoreTripDialogFragment.TAG)
    }

    private fun enableStartButton() {
        btnStartTrip.alpha = 1f
        btnStartTrip.setOnClickListener(getStartOnClickListener())
    }

    private fun disableStartButton() {
        btnStartTrip.alpha = .3f
        btnStartTrip.setOnClickListener(getDisabledOnClickListener())
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation(shouldMoveCamera: Boolean) {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (!permissionDenied) {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null && shouldMoveCamera) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), 18f))
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val KEY_LOCATION = "location"
        private const val REQUESTING_LOCATION_UPDATES = "requestingUpdates"
        private const val DIALOG_TAG = "dialog"
    }
}