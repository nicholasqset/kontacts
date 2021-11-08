package com.pango.pangodelivery.ui.auth

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.pango.pangodelivery.R
import com.pango.pangodelivery.ui.IntroSliderActivity
import com.pango.pangodelivery.ui.MainActivity
import es.dmoral.toasty.Toasty


class SplashActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: AuthStateListener? = null
    private val TAG = "SplashActivity"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val settings = this.getPreferences(Context.MODE_PRIVATE)
        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            Log.d("Comments", "First time");

            // first time task
            startActivity(Intent(this@SplashActivity, IntroSliderActivity::class.java))
            finish()

            // record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).apply();
        }


        mAuth = FirebaseAuth.getInstance()
//        mAuth!!.signOut()

        val db = Firebase.firestore

        mAuthListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {

                // User is signed in
                val docRef = db.collection("users").document(user.uid)
                docRef.get().addOnSuccessListener { it1 ->
                    if (it1 != null) {


                        Log.e("LoginActivity", "typeId " + it1.data!!["typeId"])

                        if (it1.data!!["typeId"].toString() == "5" && it1.data!!["isDelivery"].toString() == "1") {
                            val intent = Intent(this@SplashActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else if (it1.data!!["typeId"].toString() == "5" && it1.data!!["isDelivery"].toString() == "null") {
                            val intent = Intent(this@SplashActivity, DocumentsActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toasty.error(this, "User not allowed").show()
                            mAuth!!.signOut()

//

                        }


                    } else {
                        mAuth!!.signOut()

                        Log.d("MainActivity", "No such document")
                    }

                }

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