package com.pango.pangodelivery.ui

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.github.gcacace.signaturepad.views.SignaturePad
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pango.pangodelivery.databinding.ActivitySignatureBinding
import android.content.Intent
import android.graphics.Bitmap
import es.dmoral.toasty.Toasty
import java.io.ByteArrayOutputStream


class SignatureActivity : AppCompatActivity() {
    val TAG = "SignatureActivity";
    private lateinit var binding: ActivitySignatureBinding

    private var orderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignatureBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = ("Confirmation")

        val db = Firebase.firestore

        orderId = intent.getStringExtra("orderId")

        /*binding.signaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {

            }

            override fun onSigned() {
                //Event triggered when the pad is signed
                binding.saveButton.isEnabled = true
                binding.clearButton.isEnabled = true

            }

            override fun onClear() {
                binding.saveButton.isEnabled = false
                binding.clearButton.isEnabled = false
            }


        })*/

        binding.saveButton.setOnClickListener {

            val deliveryCode = binding.etDeliveryCode.editText!!.text.toString()
            val docRef: DocumentReference = db.collection("orders").document(orderId!!)
            docRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null) {
                        val dbDeliveryCode = document.getString("deliveryCode")
                        if(deliveryCode.equals(dbDeliveryCode)){

                          /*  val bitmap: Bitmap = binding.signaturePad.transparentSignatureBitmap
                            val stream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream)
                            val byteArray: ByteArray = stream.toByteArray()*/

                            val intent = Intent()
                            intent.putExtra("result", true)
                            setResult(RESULT_OK, intent)
                            finish()
                        }else{
                            Toasty.error(this, "Invalid delivery code. Possibility of wrong address.").show()
                        }
//                        Log.i("LOGGER", "First " + document.getString("first"))
//                        Log.i("LOGGER", "Last " + document.getString("last"))
//                        Log.i("LOGGER", "Born " + document.getString("born"))
                    } else {
                        Log.e("LOGGER", "No such document")
                    }
                } else {
                    Log.e("LOGGER", "get failed with ", task.exception)
                }
            }
//            val delData = mapOf(
//                "status" to 6,
//                "currentStatus" to "Delivery complete",
//                "deliveryDoneOn" to FieldValue.serverTimestamp()
//
//
//            )
//
//            db.collection("orders").document(orderId!!)
//                .update(
//                    delData
//                ).addOnSuccessListener {
//
//                    val bitmap: Bitmap = binding.signaturePad.transparentSignatureBitmap
//                    val stream = ByteArrayOutputStream()
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream)
//                    val byteArray: ByteArray = stream.toByteArray()
//
//                    val intent = Intent()
//                    intent.putExtra("result", byteArray)
//                    setResult(RESULT_OK, intent)
//                    finish()
//
////                    db.collection("onDelivery").document(uid!!)
////                        .delete()
////                        .addOnSuccessListener {
////                            dialog.dismiss()
////                            Toasty.success(
////                                this,
////                                "Delivery successfully complete",
////                                Toasty.LENGTH_LONG
////                            )
////                                .show()
////                            val intent = Intent(this, MainActivity::class.java)
////                            startActivity(intent)
////                            finish()
////                        }
//                }.addOnFailureListener { e ->
////                    dialog.dismiss()
//                    Toasty.error(this, "Something went wrong, please try again")
//                        .show()
//                    Log.w(TAG, "Error updating document", e)
//                }

//            val bitmap: Bitmap = binding.signaturePad.transparentSignatureBitmap
//            val stream = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream)
//            val byteArray: ByteArray = stream.toByteArray()
//
//            val intent = Intent()
//            intent.putExtra("result", byteArray)
//            setResult(RESULT_OK, intent)
//            finish()

        }
        binding.clearButton.setOnClickListener {
            binding.signaturePad.clear()
        }
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
}