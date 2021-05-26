package com.pango.pangodelivery.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.directions.route.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.pango.pangodelivery.R
import com.pango.pangodelivery.common.Common
import com.pango.pangodelivery.databinding.ActivityMapsBinding
import com.pango.pangodelivery.ui.auth.LoginActivity
import dmax.dialog.SpotsDialog
import es.dmoral.toasty.Toasty
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, RoutingListener, PermissionListener,
    LocationListener {

    companion object {
        const val REQUEST_CHECK_SETTINGS = 43
        const val SIGNATURE_REQUEST = 45
    }

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMapsBinding
    private lateinit var dialog: AlertDialog
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private val TAG = "MapsActivity"
    private var deliveryLatLng: LatLng? = null
    private var driverLatLng: LatLng? = null
    private var polylines: List<Polyline>? = null
    private val colors = intArrayOf(android.R.color.black)
    private var branchLat: Double = 0.0
    private var branchLng: Double = 0.0
    private var orderId: String? = null
    private var delLat: String? = null
    private var delLng: String? = null
    private var status: String? = null
    private var statusCode: Int? = null
    private var uid: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        polylines = ArrayList()
        val orderNumber = intent.getStringExtra("orderNumber")
        orderId = intent.getStringExtra("orderId")
        val storeName = intent.getStringExtra("storeName")
        val storeId = intent.getStringExtra("storeId")
        val orderDate = intent.getStringExtra("orderDate")
        val orderAmount = intent.getIntExtra("orderAmount", 0)
        val branchAddress = intent.getStringExtra("branchAddress")
        val branchEmail = intent.getStringExtra("branchEmail")
        val branchPhone = intent.getStringExtra("branchPhone")
        branchLat = intent.getDoubleExtra("branchLat", 0.0)
        branchLng = intent.getDoubleExtra("branchLng", 0.0)
        val branchImg = intent.getStringExtra("branchImg")
        val orderDelCharge = intent.getIntExtra("orderDelCharge", 0)
        val custName = intent.getStringExtra("custName")
        val custPhone = intent.getStringExtra("custPhone")
        val delAddress = intent.getStringExtra("deliveryAddress")
        delLat = intent.getStringExtra("deliveryLat")
        delLng = intent.getStringExtra("deliveryLng")
        status = intent.getStringExtra("status")
        statusCode = intent.getIntExtra("statusCode", 1)

        supportActionBar!!.title = "Order#: $orderNumber"
        supportActionBar!!.subtitle = (status)

        deliveryLatLng = LatLng(delLat!!.toDouble(), delLng!!.toDouble())
        binding.storeName.text = storeName
        binding.storeAddress.text = branchAddress
        binding.storePhone.text = branchPhone
        binding.custAddress.text = delAddress
        binding.custName.text = custName
        binding.custPhone.text = custPhone
        val db = Firebase.firestore
        mAuth = FirebaseAuth.getInstance()

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                uid = user.uid
                db.collection("onDelivery").document(uid!!)
                    .addSnapshotListener(EventListener { value, error ->
                        if (error == null) {
                            status = value!!.data!!["status"].toString()
                            statusCode = value.data!!["statusCode"].toString().toInt()

                            supportActionBar!!.subtitle = (status)
                            when (statusCode) {
                                3 -> {
                                    binding.customerDetails.visibility = View.VISIBLE
                                    binding.storeDetails.visibility = View.GONE
                                    binding.reachedOrder.visibility = View.GONE
                                    binding.pickedOrder.visibility = View.GONE
                                    binding.completeOrder.visibility = View.VISIBLE
                                }
                                2 -> {
                                    binding.reachedOrder.visibility = View.GONE
                                    binding.pickedOrder.visibility = View.VISIBLE
                                    binding.completeOrder.visibility = View.GONE

                                }
                                1 -> {
                                    binding.reachedOrder.visibility = View.VISIBLE
                                    binding.pickedOrder.visibility = View.GONE
                                    binding.completeOrder.visibility = View.GONE
                                }
                            }
                            getCurrentLocation()
                        }
                    })

            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        Log.e("MapsActivity", "branchLatLng $branchLat $branchLng")

        binding.reachedOrder.setOnClickListener {
            dialog = SpotsDialog.Builder().setContext(this).build()
            dialog.setMessage("Please wait...")
            dialog.show()
            db.collection("onDelivery").document(uid!!)
                .update(
                    mapOf(
                        "status" to "Picking up order",
                        "statusCode" to 2
                    )
                ).addOnSuccessListener {
                    db.collection("orders").document(orderId!!)
                        .update(
                            mapOf(
                                "currentStatus" to "Picking up order",
                            )
                        ).addOnSuccessListener {
                            dialog.dismiss()
                        }.addOnFailureListener { e ->
                            dialog.dismiss()
                            Toasty.error(this, "Something went wrong, please try again").show()
                            Log.w(TAG, "Error updating document", e)
                        }
                }.addOnFailureListener { e ->
                    dialog.dismiss()
                    Toasty.error(this, "Something went wrong, please try again").show()
                    Log.w(TAG, "Error updating document", e)
                }
        }
        binding.pickedOrder.setOnClickListener {
            dialog = SpotsDialog.Builder().setContext(this).build()
            dialog.setMessage("Please wait...")
            dialog.show()
            db.collection("onDelivery").document(uid!!)
                .update(
                    mapOf(
                        "status" to "Heading to customer",
                        "statusCode" to 3
                    )
                ).addOnSuccessListener {
                    db.collection("orders").document(orderId!!)
                        .update(
                            mapOf(
                                "currentStatus" to "Heading to customer",
                            )
                        ).addOnSuccessListener {
                            dialog.dismiss()
                        }.addOnFailureListener { e ->
                            dialog.dismiss()
                            Toasty.error(this, "Something went wrong, please try again").show()
                            Log.w(TAG, "Error updating document", e)
                        }
                }.addOnFailureListener { e ->
                    dialog.dismiss()
                    Toasty.error(this, "Something went wrong, please try again").show()
                    Log.w(TAG, "Error updating document", e)
                }
        }
        binding.completeOrder.setOnClickListener {
            val i = Intent(this@MapsActivity, SignatureActivity::class.java)
            startActivityForResult(i, SIGNATURE_REQUEST)
        }
    }


    override fun onMapReady(map: GoogleMap?) {
        googleMap = map ?: return
        if (isPermissionGiven()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isZoomGesturesEnabled = true


            /*try {
                // Customise the styling of the base map using a JSON object defined
                // in a raw resource file.
                val success: Boolean = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json
                    )
                )
                Log.d("MapsActivity", "Style parsing success.")
                if (!success) {
                    Log.e("MapsActivity", "Style parsing failed.")
                }
            } catch (e: NotFoundException) {
                Log.e("MapsActivity", "Can't find style. Error: ", e)
            }*/
            getCurrentLocation()
        } else {
            givePermission()
        }
    }

    private fun isPermissionGiven(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun givePermission() {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(this)
            .check()
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        getCurrentLocation()
    }

    override fun onPermissionRationaleShouldBeShown(
        permission: PermissionRequest?,
        token: PermissionToken?
    ) {
        token!!.continuePermissionRequest()
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        Toast.makeText(this, "Permission required for showing location", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun getCurrentLocation() {

        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = (10 * 1000).toLong()
        locationRequest.fastestInterval = 2000

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()

        val result = LocationServices.getSettingsClient(this).checkLocationSettings(
            locationSettingsRequest
        )
        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                if (response!!.locationSettingsStates.isLocationPresent) {
                    getLastLocation()
                }
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvable = exception as ResolvableApiException
                        resolvable.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                    } catch (e: IntentSender.SendIntentException) {
                    } catch (e: ClassCastException) {
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.lastLocation
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    val mLastLocation = task.result

                    /*var address = "No known address"

                    val gcd = Geocoder(this, Locale.getDefault())
                    val addresses: List<Address>
                    try {
                        addresses = gcd.getFromLocation(
                            mLastLocation!!.latitude,
                            mLastLocation.longitude,
                            1
                        )
                        if (addresses.isNotEmpty()) {
                            address = addresses[0].getAddressLine(0)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }*/

                    drawRoute(mLastLocation)


                } else {
                    Toast.makeText(this, "No current location found", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun drawRoute(mLastLocation: Location?) {
        val icon = BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(
                this.resources,
                R.drawable.marker
            )
        )
        googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(mLastLocation!!.latitude, mLastLocation.longitude))
                .title("Current Location")

                .icon(icon)
        )


        Log.e("MapsActivity", "LatLng ${mLastLocation.latitude} ${mLastLocation.longitude}")
        Log.e("MapsActivity", "branchLatLng $branchLat $branchLng")
        driverLatLng = LatLng(mLastLocation.latitude, mLastLocation.longitude)
        if (statusCode == 1 || statusCode == 2) {
            val routing = Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .key("AIzaSyBdGRnFBDUjXw4Aa4GQtyTCnIfzT6lwdQ4")
                .waypoints(
                    driverLatLng,
                    LatLng(branchLat, branchLng)
                )
                .build()
            routing.execute()
        } else {
            val routing = Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .key("AIzaSyBdGRnFBDUjXw4Aa4GQtyTCnIfzT6lwdQ4")
                .waypoints(
                    driverLatLng,
                    deliveryLatLng
                )
                .build()
            routing.execute()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    getCurrentLocation()
                }
            }
            SIGNATURE_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        val result: ByteArray? = data.getByteArrayExtra("result")
                        dialog = SpotsDialog.Builder().setContext(this).build()
                        dialog.setMessage("Please wait...")
                        dialog.show()

                        val time = "" + System.currentTimeMillis()
                        val storageRef = FirebaseStorage.getInstance().getReference("documents")
                            .child(uid!!)
                        val ref = storageRef.child(time)
                        ref.putBytes(result!!).addOnProgressListener {

                        }.continueWithTask {
                            ref.downloadUrl.addOnSuccessListener { url ->
                                val content: String = url.toString()
                                val db = Firebase.firestore
                                val delData = mapOf(
                                    "status" to 6,
                                    "currentStatus" to "Delivery complete",
                                    "signature" to content,
                                    "deliveryDoneOn" to FieldValue.serverTimestamp()


                                )
                                db.collection("orders").document(orderId!!)
                                    .update(
                                        delData
                                    ).addOnSuccessListener {

                                        db.collection("onDelivery").document(uid!!)
                                            .delete()
                                            .addOnSuccessListener {
                                                dialog.dismiss()
                                                Toasty.success(this, "Delivery successfully complete", Toasty.LENGTH_LONG)
                                                    .show()
                                                val intent = Intent(this,MainActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                    }.addOnFailureListener { e ->
                                        dialog.dismiss()
                                        Toasty.error(this, "Something went wrong, please try again")
                                            .show()
                                        Log.w(TAG, "Error updating document", e)
                                    }

                            }.addOnFailureListener {

                            }
                            // Request the public download URL
                            ref.downloadUrl
                        }

                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toasty.info(this, "You have cancelled taking of signature", Toasty.LENGTH_SHORT)
                        .show()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRoutingFailure(e: RouteException?) {
        if (e != null) {
            Toasty.error(this, "Error: " + e.toString(), Toast.LENGTH_LONG).show()
            Log.e("MapsActivity", "onRoutingFailure: " + e.message)
        } else {
            Toasty.error(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    override fun onRoutingStart() {

    }

    override fun onRoutingSuccess(p0: ArrayList<Route>?, p1: Int) {
        val cameraPosition = CameraPosition.Builder()
            .target(driverLatLng!!)
            .zoom(18f)
            .build()
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        if (polylines!!.isNotEmpty()) {
            for (poly in polylines!!) {
                poly.remove()
            }
        }

        polylines = ArrayList()
        for (i in 0 until p0!!.size) {

            //In case of more than 5 alternative routes
            val colorIndex: Int = i % colors.size
            val polyOptions = PolylineOptions()
            polyOptions.color(resources.getColor(colors[colorIndex]))
            polyOptions.width((10 + i * 3).toFloat())
            polyOptions.addAll(p0[i].points)
            val polyline: Polyline = googleMap.addPolyline(polyOptions)
            (polylines as ArrayList<Polyline>).add(polyline)
            binding.distance.text = "~${(p0[i].distanceValue / 1000)}Kms"
            binding.time.text = "~${p0[i].durationValue / 60}Min"

            // Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();
        }

        val d = BitmapFactory.decodeResource(resources, R.drawable.shop)
        val shopMarkerIcon = BitmapDescriptorFactory.fromBitmap(d)

        // Start marker

        // Start marker
        val options = MarkerOptions()
        options.position(LatLng(branchLat, branchLng))
        options.icon(shopMarkerIcon)
        googleMap.addMarker(options)

    }

    override fun onRoutingCancelled() {

    }

    override fun onLocationChanged(p0: Location) {
        drawRoute(p0)
    }

    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(mAuthListener!!)


    }

    override fun onStop() {
        super.onStop()
        mAuth!!.removeAuthStateListener(mAuthListener!!)


    }
}