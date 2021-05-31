package com.pango.pangodelivery.ui.auth

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pango.pangodelivery.R
import com.pango.pangodelivery.databinding.ActivityLoginBinding
import com.pango.pangodelivery.ui.MainActivity
import dmax.dialog.SpotsDialog
import es.dmoral.toasty.Toasty

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var dialog: AlertDialog
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        mAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                val docRef = db.collection("users").document(user.uid)
                docRef.get().addOnSuccessListener { it1 ->
                    if (it1 != null) {


                        Log.e("LoginActivity", "typeId " + it1.data!!["typeId"])

                        if (it1.data!!["typeId"].toString() == "5" && it1.data!!["isDelivery"].toString() == "1") {
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else if (it1.data!!["typeId"].toString() == "5" && it1.data!!["isDelivery"].toString() == "null") {
                            val intent = Intent(this@LoginActivity, DocumentsActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toasty.error(this, "User not allowed").show()
                            mAuth!!.signOut()
                            dialog.dismiss()
//

                        }


                    } else {
                        mAuth!!.signOut()
                        dialog.dismiss()
                        Log.d("MainActivity", "No such document")
                    }

                }


            }
        }

        binding.forgotBtn.setOnClickListener {
            val intent = Intent(this@LoginActivity, ResetPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.email.editText!!.text.toString()
            val password = binding.password.editText!!.text.toString()

            if (email.isEmpty()) {

                Toasty.error(this, "Email address cannot be blank", Toasty.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toasty.error(this, "Password cannot be blank", Toasty.LENGTH_LONG).show()
                return@setOnClickListener
            }

            signIn(email, password)

        }


    }

    private fun signIn(email: String, password: String) {
        dialog = SpotsDialog.Builder().setContext(this).build()
        dialog.show()

        mAuth!!.signInWithEmailAndPassword(
            email,
            password
        )
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    dialog.dismiss()
                    Toasty.success(
                        this@LoginActivity, "Sign in successful.",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    // If sign in fails, display a message to the user.
                    dialog.dismiss()
                    Log.e("LoginActivity", "signInWithEmail:failure", task.exception)
                    Toasty.error(
                        this@LoginActivity,
                        "Sign in failed, Please check email and password then try again",
                        Toast.LENGTH_LONG
                    ).show()

                }


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