package com.pango.pangodelivery.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.withEmail
import com.mikepenz.materialdrawer.model.interfaces.withIcon
import com.mikepenz.materialdrawer.model.interfaces.withIdentifier
import com.mikepenz.materialdrawer.model.interfaces.withName
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.mikepenz.materialdrawer.util.addStickyFooterItem
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import com.pango.pangodelivery.R
import com.pango.pangodelivery.databinding.ActivityMainBinding
import com.pango.pangodelivery.ui.auth.LoginActivity
import com.pango.pangodelivery.ui.fragment.*
import dmax.dialog.SpotsDialog
import nl.joery.animatedbottombar.AnimatedBottomBar
import java.util.*

class MainActivity : AppCompatActivity(), PermissionListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dialog: AlertDialog
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private val TAG = "MainActivity"
    private lateinit var headerView: AccountHeaderView
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private var uid: String? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var driverLatLng: LatLng? = null
    private var savedInstance: Bundle? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

        mAuth = FirebaseAuth.getInstance()
        savedInstance = savedInstanceState

        val dashFragment = DashboardFragment()
        val earnFragment = EarningsFragment()
        val accFragment = MyAccountFragment()
        val availableFragment = AvailableFragment()
        val fragsList = listOf(dashFragment, earnFragment, accFragment)
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        if (isPermissionGiven()) {
            getCurrentLocation()
        } else {
            givePermission()
        }

        actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            binding.root,
            binding.toolbar,
            com.mikepenz.materialdrawer.R.string.material_drawer_open,
            com.mikepenz.materialdrawer.R.string.material_drawer_close
        )
        binding.root.addDrawerListener(actionBarDrawerToggle)
        val db = Firebase.firestore

        dialog = SpotsDialog.Builder().setContext(this).build()
        dialog.setMessage("Please wait...")
        dialog.show()

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                uid = user.uid

                /*db.collection("onDelivery").document(uid!!).get().addOnSuccessListener {
                    dialog.dismiss()
                    if (it.exists()) {

                        val branchId = it.data!!["branchId"].toString()
                        val branchName = it.data!!["branchName"].toString()
                        val branchAddress = it.data!!["branchAddress"].toString()
                        val branchImg = it.data!!["branchImg"].toString()
                        val branchEmail = it.data!!["branchEmail"].toString()
                        val branchPhone = it.data!!["branchPhone"].toString()
                        val branchLat = it.data!!["branchLat"].toString().toDouble()
                        val branchLng = it.data!!["branchLng"].toString().toDouble()
                        val orderNumber = it.data!!["orderNumber"].toString()
                        val orderId = it.data!!["orderId"].toString()
                        val orderDate = it.data!!["orderDate"].toString()
                        val orderDelCharge = it.data!!["orderDelCharge"].toString()
                        val status = it.data!!["status"].toString()
                        val statusCode = it.data!!["statusCode"].toString().toDouble()
                        val orderAmount = it.data!!["orderAmount"].toString().toDouble()
                        val custName = it.data!!["custName"].toString()
                        val custPhone = it.data!!["custPhone"].toString()
                        val delAddress = it.data!!["deliveryAddress"].toString()
                        val delLat = it.data!!["deliveryLat"].toString()
                        val delLng = it.data!!["deliveryLng"].toString()


                        val intent = Intent(this, MapsActivity::class.java)
                        intent.putExtra("orderNumber", orderNumber)
                        intent.putExtra("orderId", orderId)
                        intent.putExtra("storeName", branchName)
                        intent.putExtra("storeId", branchId)
                        intent.putExtra("orderDate", orderDate)
                        intent.putExtra("orderAmount", orderAmount)
                        intent.putExtra("branchAddress", branchAddress)
                        intent.putExtra("branchEmail", branchEmail)
                        intent.putExtra("branchPhone", branchPhone)
                        intent.putExtra("branchLat", branchLat)
                        intent.putExtra("branchLng", branchLng)
                        intent.putExtra("branchImg", branchImg)
                        intent.putExtra("orderDelCharge", orderDelCharge)
                        intent.putExtra("status", status)
                        intent.putExtra("statusCode", statusCode)
                        intent.putExtra("custName",custName)
                        intent.putExtra("custPhone", custPhone)
                        intent.putExtra("deliveryAddress", delAddress)
                        intent.putExtra("deliveryLat", delLat)
                        intent.putExtra("deliveryLng", delLng)
                        startActivity(intent)


                    }
                }*/

                val docRef = db.collection("users").document(uid!!)
                docRef.get().addOnSuccessListener {
                    if (it != null) {
                        dialog.dismiss()
                        Log.d("MainActivity", "DocumentSnapshot data: ${it.data}")
                        val sharedPref = getSharedPreferences("PangoDelivery", Context.MODE_PRIVATE)
                        val editor: SharedPreferences.Editor = sharedPref.edit()
                        editor.putString("myId", uid)
                        editor.putString("name", it.data!!["displayName"].toString())
                        editor.putString("phone", it.data!!["phoneNumber"].toString())
                        editor.putString("email", it.data!!["email"].toString())
                        editor.putString("pic", it.data!!["userPhoto"].toString())
                        editor.apply()
                        editor.commit()

                        // Create the AccountHeader
                        headerView = AccountHeaderView(this).apply {
                            attachToSliderView(binding.slider) // attach to the slider
                            addProfiles(
                                ProfileDrawerItem().withIdentifier(0)
                                    .withName(it.data!!["displayName"].toString())
                                    .withEmail(user.email)
                                    .withIcon(it.data!!["userPhoto"].toString())
                            )
                            onAccountHeaderListener = { view, profile, current ->
                                // react to profile changes
                                false
                            }
                            withSavedInstance(savedInstanceState)
                        }
                    }
                }


            } else {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(
                imageView: ImageView,
                uri: Uri,
                placeholder: Drawable,
                tag: String?
            ) {
                super.set(imageView, uri, placeholder, tag)
                Glide.with(imageView.context).load(uri).into(imageView)
            }

            override fun cancel(imageView: ImageView) {
                super.cancel(imageView)
            }
        })

        //if you want to update the items at a later time it is recommended to keep it in a variable
        val item1 =
            PrimaryDrawerItem().withIdentifier(1).withName("Dashboard")
                .withIcon(resources.getDrawable(R.drawable.ic_home))

        val item4 = PrimaryDrawerItem().withIdentifier(4).withName("Terms & Conditions")
            .withIcon(resources.getDrawable(R.drawable.ic_terms_and_conditions))
        val item5 = PrimaryDrawerItem().withIdentifier(5).withName("Privacy Policy")
            .withIcon(resources.getDrawable(R.drawable.ic_accept))
        val item6 = PrimaryDrawerItem().withIdentifier(6).withName("About us")
            .withIcon(resources.getDrawable(R.drawable.ic_about))
        val item7 = PrimaryDrawerItem().withIdentifier(7).withName("Settings")
            .withIcon(resources.getDrawable(R.drawable.ic_settings))
        val item8 = PrimaryDrawerItem().withIdentifier(8).withName("Support")
            .withIcon(resources.getDrawable(R.drawable.ic_support))
        val item10 = PrimaryDrawerItem().withIdentifier(10).withName("Sign Out")
            .withIcon(resources.getDrawable(R.drawable.ic_baseline_exit_to_app_black_24))
        // get the reference to the slider and add the items
        binding.slider.itemAdapter.add(
            item1,
            item4,
            item5,
            item6,
            item7,
            item8,
            item10
        )
        binding.slider.onDrawerItemClickListener = { v, drawerItem, position ->
            // do something with the clicked item :D
            when (position) {

                1 -> {
                }
                2 -> {

                }
                3 -> {

                }
                4 -> {

                }
                5 -> {

                }
                6 -> {

                }
                7 -> {
                    mAuth!!.signOut()
                }


            }
            false
        }
        binding.slider.setSelection(1)
        binding.slider.addStickyFooterItem(
            PrimaryDrawerItem().withIdentifier(9).withName("Powered by Pango Africa Ltd")
        )


        binding.bottomBar.setOnTabSelectListener(object : AnimatedBottomBar.OnTabSelectListener {
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                Log.e("bottom_bar", "Selected index: $newIndex, title: ${newTab.title}")

                when (newIndex) {
                    0 -> {
                        supportActionBar!!.title = "Available Orders"
                        val bundle = Bundle()
                        bundle.putString("uid", uid)
                        bundle.putDouble("myLat", driverLatLng!!.latitude)
                        bundle.putDouble("myLng", driverLatLng!!.longitude)

                        availableFragment.arguments = bundle
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, availableFragment)
                            .commit()
                    }

                    1 -> {

                        supportActionBar!!.title = "Dashboard"
                        val bundle = Bundle()
                        bundle.putString("uid", uid)
                        dashFragment.arguments = bundle
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, dashFragment)
                            .commit()

                    }
                    2 -> {
                        supportActionBar!!.title = "Earnings"
                        val bundle = Bundle()
                        bundle.putString("uid", uid)
                        earnFragment.arguments = bundle
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, earnFragment)
                            .commit()

                    }
                    /*3 -> {
                        supportActionBar!!.title = "My Account"
                        val bundle = Bundle()
                        bundle.putString("uid", uid)
                        ordersFragment.arguments = bundle
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, accFragment)
                            .commit()


                    }*/


                }


            }

            // An optional method that will be fired whenever an already selected tab has been selected again.
            override fun onTabReselected(index: Int, tab: AnimatedBottomBar.Tab) {
                Log.e("bottom_bar", "Reselected index: $index, title: ${tab.title}")
            }
        })


    }

    class MyCustomPagerAdapter(
        activity: FragmentActivity,
        private val fragments: List<Fragment>
    ) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }

    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(mAuthListener!!)
    }

    override fun onStop() {
        super.onStop()
        mAuth!!.removeAuthStateListener(mAuthListener!!)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(_outState: Bundle) {
        var outState = _outState
        //add the values which need to be saved from the drawer to the bundle
        outState = binding.slider.saveInstanceState(outState)

        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerView.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }


    override fun onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (binding.root.isDrawerOpen(binding.slider)) {
            binding.root.closeDrawer(binding.slider)
        } else {
            super.onBackPressed()
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onResume() {
        super.onResume()
        actionBarDrawerToggle.syncState()
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
                        resolvable.startResolutionForResult(
                            this,
                            MapsActivity.REQUEST_CHECK_SETTINGS
                        )
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
                    Log.e(
                        "MainActivity",
                        "LatLng ${mLastLocation.latitude} ${mLastLocation.longitude}"
                    )
                    driverLatLng = LatLng(mLastLocation.latitude, mLastLocation.longitude)
                    if (savedInstance == null) {
                        supportActionBar!!.title = "Available Orders"
                        val availableFragment = AvailableFragment()
                        val bundle = Bundle()
                        bundle.putString("uid", uid)
                        bundle.putDouble("myLat", driverLatLng!!.latitude)
                        bundle.putDouble("myLng", driverLatLng!!.longitude)
                        availableFragment.arguments = bundle
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, availableFragment)
                            .commit()
                    }

                } else {
                    Toast.makeText(this, "No current location found", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    getCurrentLocation()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)

    }

    companion object {
        const val REQUEST_CHECK_SETTINGS = 44
    }
}