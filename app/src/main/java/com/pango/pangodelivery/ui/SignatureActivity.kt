package com.pango.pangodelivery.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.github.gcacace.signaturepad.views.SignaturePad
import com.pango.pangodelivery.databinding.ActivitySignatureBinding
import java.io.ByteArrayOutputStream


class SignatureActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignatureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignatureBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = ("Confirmation")

        binding.signaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
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


        })

        binding.saveButton.setOnClickListener {
            val bitmap: Bitmap = binding.signaturePad.transparentSignatureBitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream)
            val byteArray: ByteArray = stream.toByteArray()

            val intent = Intent()
            intent.putExtra("result", byteArray)
            setResult(RESULT_OK, intent)
            finish()

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