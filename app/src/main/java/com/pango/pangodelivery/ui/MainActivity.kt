package com.pango.pangodelivery.ui

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
import com.pango.pangodelivery.ui.fragment.OrdersFragment
import com.pango.pangodelivery.R
import com.pango.pangodelivery.databinding.ActivityMainBinding
import com.pango.pangodelivery.ui.auth.LoginActivity
import com.pango.pangodelivery.ui.fragment.DashboardFragment
import com.pango.pangodelivery.ui.fragment.EarningsFragment
import com.pango.pangodelivery.ui.fragment.MyAccountFragment
import nl.joery.animatedbottombar.AnimatedBottomBar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dialog: AlertDialog
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private val TAG = "MainActivity"
    private lateinit var headerView: AccountHeaderView
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private var uid: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

        mAuth = FirebaseAuth.getInstance()


        val dashFragment = DashboardFragment()
        val earnFragment = EarningsFragment()
        val accFragment = MyAccountFragment()
        val ordersFragment = OrdersFragment()
        val fragsList = listOf(dashFragment, earnFragment, accFragment)

        actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            binding.root,
            binding.toolbar,
            com.mikepenz.materialdrawer.R.string.material_drawer_open,
            com.mikepenz.materialdrawer.R.string.material_drawer_close
        )
        binding.root.addDrawerListener(actionBarDrawerToggle)
        val db = Firebase.firestore
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                uid = user.uid
                if (savedInstanceState == null) {
                    supportActionBar!!.title = "Available Orders"

                    val bundle = Bundle()
                    bundle.putString("uid", uid)
                    ordersFragment.arguments = bundle
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, ordersFragment)
                        .commit()
                }
                val docRef = db.collection("users").document(uid!!)
                docRef.get().addOnSuccessListener {
                    if (it != null) {
                        Log.d("MainActivity", "DocumentSnapshot data: ${it.data}")
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
                7 ->{
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
                        ordersFragment.arguments = bundle
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, ordersFragment)
                            .commit()
                    }

                    1 -> {

                        supportActionBar!!.title = "Dashboard"
                        val bundle = Bundle()
                        bundle.putString("uid", uid)
                        ordersFragment.arguments = bundle
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, dashFragment)
                            .commit()

                    }
                    2 -> {
                        supportActionBar!!.title = "Earnings"
                        val bundle = Bundle()
                        bundle.putString("uid", uid)
                        ordersFragment.arguments = bundle
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, earnFragment)
                            .commit()

                    }
                    3 -> {
                        supportActionBar!!.title = "My Account"
                        val bundle = Bundle()
                        bundle.putString("uid", uid)
                        ordersFragment.arguments = bundle
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, accFragment)
                            .commit()


                    }


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
}