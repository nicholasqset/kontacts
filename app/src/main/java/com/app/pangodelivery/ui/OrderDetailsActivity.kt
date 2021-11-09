package com.app.pangodelivery.ui

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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.app.pangodelivery.R
import com.app.pangodelivery.common.Common
import com.app.pangodelivery.databinding.ActivityOrderDetailsBinding
import com.app.pangodelivery.model.Item
import com.app.pangodelivery.ui.auth.LoginActivity
import com.app.pangodelivery.viewholder.ItemViewHolder
import dmax.dialog.SpotsDialog
import es.dmoral.toasty.Toasty


class OrderDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderDetailsBinding
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
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
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
        val distance = intent.getIntExtra("distance", 0)



        supportActionBar!!.title = "Order#: $orderNumber"
        binding.amount.text = "Kshs $orderAmount"
        binding.commission.text = "Kshs $orderDelCharge"
        binding.storeAddress.text = branchAddress
        binding.storeName.text = storeName
        binding.date.text = orderDate
        binding.distance.text = "~${distance}Kms"
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
        binding.acceptOrder.setOnClickListener {

            dialog = SpotsDialog.Builder().setContext(this).build()
            dialog.setMessage("Please wait...")
            dialog.show()
            val deliveryDetails = hashMapOf(
                "orderNumber" to orderNumber,
                "orderId" to orderId,
                "branchName" to storeName,
                "branchId" to storeId,
                "orderDate" to orderDate,
                "orderAmount" to orderAmount,
                "branchAddress" to branchAddress,
                "branchEmail" to branchEmail,
                "branchPhone" to branchPhone,
                "branchLat" to branchLat,
                "branchLng" to branchLng,
                "branchImg" to branchImg,
                "orderDelCharge" to orderDelCharge,
                "status" to "On the way to the shop",
                "driverId" to uid,
                "statusCode" to 1,
                "timestamp" to FieldValue.serverTimestamp(),
                "custName" to custName,
                "custPhone" to custPhone,
                "deliveryAddress" to delAddress,
                "deliveryLat" to delLat,
                "deliveryLng" to delLng,
                "deliveryByName" to Common(this).myName,
                "deliveryByPhone" to Common(this).myPhone,
                "deliveryByPhoto" to Common(this).myPic

            )
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("orderNumber", orderNumber)
            intent.putExtra("orderId", orderId)
            intent.putExtra("storeName", storeName)
            intent.putExtra("storeId", storeId)
            intent.putExtra("orderDate", orderDate)
            intent.putExtra("orderAmount", orderAmount)
            intent.putExtra("branchAddress", branchAddress)
            intent.putExtra("branchEmail", branchEmail)
            intent.putExtra("branchPhone", branchPhone)
            intent.putExtra("branchLat", branchLat)
            intent.putExtra("branchLng", branchLng)
            intent.putExtra("branchImg", branchImg)
            intent.putExtra("orderDelCharge", orderDelCharge)
            intent.putExtra("status", "On the way to the shop")
            intent.putExtra("statusCode", 1)
            intent.putExtra("custName",custName)
            intent.putExtra("custPhone", custPhone)
            intent.putExtra("deliveryAddress", delAddress)
            intent.putExtra("deliveryLat", delLat)
            intent.putExtra("deliveryLng", delLng)
            db.collection("orders").document(orderId).get().addOnSuccessListener {
                if (it.data!!["status"].toString() != "3"){
                    dialog.dismiss()
                    Toasty.info(this, "Order has been taken by another delivery person, please try another order", Toasty.LENGTH_LONG).show()
                    finish()
                }else{
                    db.collection("onDelivery").document(orderId)
                        .set(deliveryDetails)
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully written!")
                            db.collection("orders").document(orderId)
                                .update(
                                    mapOf(
                                        "status" to 3,
                                        "currentStatus" to "On the way to the shop",
                                        "deliveryStartedOn" to FieldValue.serverTimestamp(),
                                        "deliveryByName" to Common(this).myName,
                                        "deliveryByPhone" to Common(this).myPhone,
                                        "deliveryByPhoto" to Common(this).myPic,
                                        "deliveryById" to uid
                                    )
                                ).addOnSuccessListener {
                                    dialog.dismiss()
                                    startActivity(intent)
                                }.addOnFailureListener { e ->
                                    dialog.dismiss()
                                    Toasty.error(this, "Something went wrong, please try again").show()
                                    Log.w(TAG, "Error updating document", e)
                                }
                        }
                        .addOnFailureListener { e ->
                            dialog.dismiss()
                            Toasty.error(this, "Something went wrong, please try again").show()
                            Log.w(TAG, "Error writing document", e)
                        }
                }

            }



        }

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
                Glide.with(this@OrderDetailsActivity).load(Item.itemImage).into(holder.image)


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