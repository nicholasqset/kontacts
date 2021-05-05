package com.pango.pangodelivery.ui.auth

import android.Manifest
import android.R.attr.name
import android.R.id
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.pango.pangodelivery.R
import com.pango.pangodelivery.ui.MainActivity


class SplashActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: AuthStateListener? = null
    private val TAG = "SplashActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mAuth = FirebaseAuth.getInstance()


        val permissionlistener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {}
            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(
                    this@SplashActivity,
                    "Permission Denied\n$deniedPermissions",
                    Toast.LENGTH_SHORT
                )
                    .show()
                finish()
                moveTaskToBack(true) // finish activity
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedTitle("Permission denied")
                .setDeniedMessage(
                    "If you reject permission, you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]"
                )
                .setPermissions(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .check()
        } else {
            TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedTitle("Permission denied")
                .setDeniedMessage(
                    "If you reject permission, you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]"
                )
                .setPermissions(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE
                )
                .check()
        }

        mAuthListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {

                // User is signed in
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()

            } else {
                val mainIntent = Intent(this@SplashActivity, LoginActivity::class.java)
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(mainIntent)
                finish()
                Log.d(TAG, "onAuthStateChanged:signed_out")
            }
            // ...
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
}