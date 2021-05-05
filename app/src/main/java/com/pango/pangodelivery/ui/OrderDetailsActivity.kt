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
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pango.pangodelivery.R
import com.pango.pangodelivery.databinding.ActivityOrderDetailsBinding
import com.pango.pangodelivery.model.Item
import com.pango.pangodelivery.viewholder.ItemViewHolder


class OrderDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderDetailsBinding
    private lateinit var dialog: AlertDialog
    private var adapter: FirestoreRecyclerAdapter<Item, ItemViewHolder>? = null
    private var firestoreListener: ListenerRegistration? = null
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
        val orderNumber = intent.getStringExtra("orderNumber")
        val orderId = intent.getStringExtra("orderId")
        val storeName = intent.getStringExtra("storeName")
        val storeId = intent.getStringExtra("storeId")
        val orderDate = intent.getStringExtra("orderDate")
        val orderAmount = intent.getIntExtra("orderAmount", 0)
        val branchAddress = intent.getStringExtra("branchAddress")
        val branchEmail = intent.getStringExtra("branchEmail")
        val branchPhone = intent.getStringExtra("branchPhone")
        val branchLat = intent.getDoubleExtra("branchLat",0.0)
        val branchLng = intent.getDoubleExtra("branchLng",0.0)
        val branchImg = intent.getStringExtra("branchImg")
        val orderDelCharge = intent.getIntExtra("orderDelCharge", 0)


        supportActionBar!!.title = "Order#: $orderNumber"
        binding.amount.text = "Kshs $orderAmount"
        binding.commission.text = "Kshs $orderDelCharge"
        binding.storeAddress.text = branchAddress
        binding.storeName.text = storeName
        binding.date.text = orderDate
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
                        val Item = doc.toObject(Item::class.java)
                        Item.id = doc.id
                        itemList.add(Item)

                    }
                    Log.e("MainActivity", "Listen success! $itemList")


                })
        binding.acceptOrder.setOnClickListener {
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
            startActivity(intent)
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


    }

    override fun onStop() {
        super.onStop()
        adapter!!.stopListening()


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