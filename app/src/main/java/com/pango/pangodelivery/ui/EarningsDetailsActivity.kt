package com.pango.pangodelivery.ui

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pango.pangodelivery.R
import com.pango.pangodelivery.databinding.ActivityEarningsDetailsBinding
import com.pango.pangodelivery.model.Item
import com.pango.pangodelivery.ui.auth.LoginActivity
import com.pango.pangodelivery.viewholder.ItemViewHolder


class EarningsDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEarningsDetailsBinding
    private lateinit var dialog: AlertDialog
    private var adapter: FirestoreRecyclerAdapter<Item, ItemViewHolder>? = null
    private var firestoreListener: ListenerRegistration? = null
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var itemList = mutableListOf<Item>()
    private val TAG = "OrderDetailsActivity"
    private var uid: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEarningsDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mAuth = FirebaseAuth.getInstance()

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                uid = user.uid


            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        val orderNumber = intent.getStringExtra("orderNumber")
        val orderId = intent.getStringExtra("orderId")
        val storeName = intent.getStringExtra("storeName")
        val storeId = intent.getStringExtra("storeId")
        val orderDate = intent.getStringExtra("orderDate")
        val orderAmount = intent.getIntExtra("orderAmount", 0)
        val branchAddress = intent.getStringExtra("branchAddress")
        val branchEmail = intent.getStringExtra("branchEmail")
        val branchPhone = intent.getStringExtra("branchPhone")
        val branchLat = intent.getDoubleExtra("branchLat", 0.0)
        val branchLng = intent.getDoubleExtra("branchLng", 0.0)
        val branchImg = intent.getStringExtra("branchImg")
        val orderDelCharge = intent.getIntExtra("orderDelCharge", 0)
        val custName = intent.getStringExtra("custName")
        val custPhone = intent.getStringExtra("custPhone")
        val delAddress = intent.getStringExtra("deliveryAddress")
        val delLat = intent.getStringExtra("deliveryLat")
        val delLng = intent.getStringExtra("deliveryLng")
        val startDate = intent.getStringExtra("startedOn")
        val completedDate = intent.getStringExtra("completedOn")



        supportActionBar!!.title = "Order#: $orderNumber"
        binding.amount.text = "Kshs $orderAmount"
        binding.commission.text = "Kshs $orderDelCharge"
        binding.storeAddress.text = branchAddress
        binding.storeName.text = storeName
        binding.date.text = orderDate
        binding.startedOn.text = startDate
        binding.completedOn.text = completedDate
        Glide.with(this).load(branchImg).into(binding.storePic)

        val mLayoutManager = LinearLayoutManager(this)
        binding.listItems.layoutManager = mLayoutManager
        binding.listItems.itemAnimator = DefaultItemAnimator()
        val db = Firebase.firestore
        loadItemList(db, orderId)
        firestoreListener =
            db.collection("orders").document(orderId!!)
                .collection("items")
                .addSnapshotListener(EventListener { documentSnapshots, e ->
                    if (e != null) {
                        Log.e("MainActivity", "Listen failed!", e)
                        return@EventListener
                    }

                    itemList = ArrayList()

                    for (doc in documentSnapshots!!) {
                        val item = doc.toObject(Item::class.java)
                        item.id = doc.id
                        itemList.add(item)

                    }
                    Log.e("MainActivity", "Listen success! $itemList")


                })



    }

    private fun loadItemList(db: FirebaseFirestore, id: String?) {
        val query = db.collection("orders").document(id!!)
            .collection("items")


        val response = FirestoreRecyclerOptions.Builder<Item>()
            .setQuery(query, Item::class.java)
            .build()

        adapter = object : FirestoreRecyclerAdapter<Item, ItemViewHolder>(response) {

            override fun onBindViewHolder(
                holder: ItemViewHolder,
                position: Int,
                model: Item
            ) {

                val Item = itemList[position]
                holder.title.text = Item.item
                holder.price.text = "Kshs ${(Item.price!!.toInt()) * (Item.quantity!!)}"
                holder.quant.text = "${Item.quantity!!}"
                Glide.with(this@EarningsDetailsActivity).load(Item.itemImage).into(holder.image)


            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_row, parent, false)
                return ItemViewHolder(view)
            }


        }
        adapter!!.notifyDataSetChanged()
        binding.listItems.adapter = adapter

    }

    override fun onDestroy() {
        super.onDestroy()
        firestoreListener!!.remove()
    }

    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
        mAuth!!.addAuthStateListener(mAuthListener!!)


    }

    override fun onStop() {
        super.onStop()
        adapter!!.stopListening()
        mAuth!!.removeAuthStateListener(mAuthListener!!)


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