package com.pango.pangodelivery.ui.auth

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker
import com.pango.pangodelivery.databinding.ActivityDocumentsBinding
import com.pango.pangodelivery.ui.MainActivity
import com.yalantis.ucrop.UCrop
import dmax.dialog.SpotsDialog
import es.dmoral.toasty.Toasty
import java.io.File
import java.security.SecureRandom
import java.util.*


class DocumentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDocumentsBinding
    private lateinit var dialog: AlertDialog
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private val TAG = "DocumentsActivity"
    private var imageUri: Uri? = null
    private var file: File? = null
    private var downloadUrl: String? = null
    private var uid: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        mAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Add documents"
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                uid = user.uid
                val docRef = db.collection("users").document(user.uid)
                docRef.get().addOnSuccessListener { it1 ->
                    if (it1 != null) {
                        Log.e(
                            "LoginActivity",
                            "DocumentSnapshot datax: ${it1.data!!["typeId"]}"
                        )

                        if (it1.data!!["typeId"].toString() == "5" && it1.data!!["isDelivery"].toString() == "1") {
                            val intent = Intent(this@DocumentsActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else if (it1.data!!["typeId"].toString() == "5" && it1.data!!["isDelivery"].toString() == "null") {

                        }else {
                            Toasty.error(this, "User not allowed").show()
                            mAuth!!.signOut()

//

                        }


                    } else {
                        mAuth!!.signOut()
                        dialog.dismiss()
                        Log.d("MainActivity", "No such document")
                    }

                }


            } else {
                val intent = Intent(this@DocumentsActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding.drivingLicense.setOnClickListener {
            ImagePicker.with(this)
                .setFolderMode(true)
                .setFolderTitle("Album")
                .setRootDirectoryName(Config.ROOT_DIR_DCIM)
                .setDirectoryName("Pango images")
                .setMultipleMode(false)
                .setShowNumberIndicator(true)
                .setMaxSize(1)
                .setLimitMessage("You can select only one image")
                .setRequestCode(100)
                .start();
        }
        binding.saveBtn.setOnClickListener {
            val regNumber = binding.regNum.editText!!.text.toString()
            val licenseNumber = binding.licenseNum.editText!!.text.toString()

            if (regNumber.isEmpty()) {

                Toasty.error(this, "Registration number cannot be blank", Toasty.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (licenseNumber.isEmpty()) {

                Toasty.error(this, "Driving license number cannot be blank", Toasty.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }
            if (imageUri == null) {
                Toasty.error(
                    this,
                    "Driving license image cannot be blank.",
                    Toast.LENGTH_LONG,
                    true
                )
                    .show()

                return@setOnClickListener
            }

            saveDocuments(regNumber, imageUri!!, licenseNumber)
        }
    }

    private fun saveDocuments(regNumber: String, imageUri: Uri, licenseNumber: String) {
        dialog = SpotsDialog.Builder().setContext(this).build()
        dialog.show()
        val storageRef = FirebaseStorage.getInstance().reference

        val ref = storageRef.child(
            "images/" + rand(
                1,
                20
            ) + "${imageUri.lastPathSegment}"
        )
        val uploadTask = ref.putFile(imageUri)

        val urlTask = uploadTask.continueWithTask { task2 ->
            if (!task2.isSuccessful) {
                task2.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                downloadUrl = it.result.toString()
                Log.e("RegisterActivity", downloadUrl!!)

                val db = Firebase.firestore
                val delData = mapOf(
                    "isDelivery" to 1,
                    "licenseImage" to downloadUrl,
                    "vehicleNumber" to regNumber,
                    "licenseNumber" to licenseNumber,
                    "isDeliveryStartDate" to FieldValue.serverTimestamp(),
                    "active" to 1
                )
                db.collection("users").document(uid!!)
                    .update(
                        delData
                    ).addOnSuccessListener {
                        dialog.dismiss()
                        Toasty.success(
                            this,
                            "Saving documents successful",
                            Toasty.LENGTH_LONG
                        )
                            .show()
                        val intent = Intent(this@DocumentsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    }.addOnFailureListener { e ->
                        dialog.dismiss()
                        Toasty.error(this, "Something went wrong, please try again")
                            .show()
                        Log.e(TAG, "Error updating document", e)
                    }
            }
        }
    }

    private fun rand(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        val random = SecureRandom()
        random.setSeed(random.generateSeed(20))

        return random.nextInt(end - start + 1) + start
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // The last parameter value of shouldHandleResult() is the value we pass to setRequestCode().
        // If we do not call setRequestCode(), we can ignore the last parameter.
        if (ImagePicker.shouldHandleResult(requestCode, resultCode, data, 100)) {
            val images: ArrayList<Image> = ImagePicker.getImages(data)
            // Do stuff with image's path or id. For example:

            for (image in images) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    imageUri = image.uri

                    startCrop(imageUri!!)
                    /*  Glide.with(this)
                          .load(image.uri)
                          .into(userPhoto)*/
                } else {
                    file = File(image.path)
                    startCrop(Uri.fromFile(file))
                    /* Glide.with(this)
                         .load(image.path)
                         .into(userPhoto)*/
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            Glide.with(this)
                .load(resultUri)
                .into(binding.drivingLicense)
            imageUri = resultUri
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Log.e("RegisterActivity", "Crop error:", cropError)
        }
    }

    private fun startCrop(imageUri: Uri) {
        val destinationFileName =
            StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString()

        UCrop.of(imageUri, Uri.fromFile(File(cacheDir, destinationFileName)))
            .start(this)

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