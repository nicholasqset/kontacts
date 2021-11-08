package com.pango.pangodelivery.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.pango.pangodelivery.adapter.IntroSliderAdapter
import com.pango.pangodelivery.databinding.ActivityIntroSliderBinding
import com.pango.pangodelivery.ui.fragment.Intro1Fragment

class IntroSliderActivity : AppCompatActivity() {

    private val fragmentList = ArrayList<Fragment>()
    private lateinit var binding: ActivityIntroSliderBinding
    private lateinit var permissionlistener: PermissionListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // making the status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        binding = ActivityIntroSliderBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {
                val intent = Intent(this@IntroSliderActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(
                    this@IntroSliderActivity,
                    "Permission Denied\n$deniedPermissions",
                    Toast.LENGTH_SHORT
                )
                    .show()
                finish()
                moveTaskToBack(true) // finish activity
            }
        }

        val adapter = IntroSliderAdapter(this)
        binding.vpIntroSlider.adapter = adapter

        fragmentList.addAll(listOf(
            Intro1Fragment()
        ))
        adapter.setFragmentList(fragmentList)

        binding.indicatorLayout.setIndicatorCount(adapter.itemCount)
        binding.indicatorLayout.selectCurrentPosition(0)

        registerListeners()
    }

    private fun registerListeners() {
        binding.vpIntroSlider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                binding.indicatorLayout.selectCurrentPosition(position)

                if (position < fragmentList.lastIndex) {
                    binding.tvSkip.visibility = View.VISIBLE
                    binding.tvNext.text = "Next"
                } else {
                    binding.tvSkip.visibility = View.GONE
                    binding.tvNext.text = "Turn On"
                }
            }
        })

        binding.tvSkip.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.tvNext.setOnClickListener {
            val position = binding.vpIntroSlider.currentItem

            if (position < fragmentList.lastIndex) {
                binding.vpIntroSlider.currentItem = position + 1
            } else {
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
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        .check()
                }
            }
        }
    }
}
