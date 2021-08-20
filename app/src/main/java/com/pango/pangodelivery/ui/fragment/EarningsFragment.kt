package com.pango.pangodelivery.ui.fragment

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pango.pangodelivery.R

import com.pango.pangodelivery.databinding.FragmentEarningsBinding


import com.pango.pangodelivery.model.Order
import com.pango.pangodelivery.ui.EarningsDetailsActivity
import com.pango.pangodelivery.ui.OrderDetailsActivity
import com.pango.pangodelivery.viewholder.EarningsViewHolder
import com.pango.pangodelivery.viewholder.OrderViewHolder
import java.text.SimpleDateFormat
import kotlin.math.roundToInt


class EarningsFragment : Fragment() {
    private var firestoreListener: ListenerRegistration? = null
    private lateinit var orderList: ArrayList<Order>
    private var uid: String? = null
    private var adapter: FirestoreRecyclerAdapter<Order, EarningsViewHolder>? = null
    private var _binding: FragmentEarningsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEarningsBinding.inflate(inflater, container, false)
        val v = binding.root
        uid = requireArguments().getString("uid")
        val db = Firebase.firestore
        orderList = ArrayList()
        val mLayoutManager = LinearLayoutManager(v.context)
        binding.listEarnings.layoutManager = mLayoutManager
        binding.listEarnings.itemAnimator = DefaultItemAnimator()
        loadItemsList(db, v)
        firestoreListener = db.collection("orders")
            .whereEqualTo("status", 6)
            .whereEqualTo("deliveryById", uid)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener(EventListener { documentSnapshots, e ->
                if (e != null) {
                    Log.e("MainActivity", "Listen failed!", e)
                    return@EventListener
                }

                orderList = ArrayList()
                if (!documentSnapshots!!.isEmpty) {
                    for (doc in documentSnapshots) {
                        val order = doc.toObject(Order::class.java)
                        order.id = doc.id
                        orderList.add(order)

                    }
                }
                Log.e("MainActivity", "Listen success! $orderList")

            })

        return  v
    }
    private fun loadItemsList(db: FirebaseFirestore, v: View) {
        try {
            val query =
                db.collection("orders")
                    .whereEqualTo("status", 6)
                    .whereEqualTo("deliveryById", uid)
                    .orderBy("timestamp", Query.Direction.DESCENDING)

            val response = FirestoreRecyclerOptions.Builder<Order>()
                .setQuery(query, Order::class.java)
                .build()

            adapter = object : FirestoreRecyclerAdapter<Order, EarningsViewHolder>(response) {

                override fun onBindViewHolder(
                    holder: EarningsViewHolder,
                    position: Int,
                    model: Order
                ) {

                    if (orderList.size != 0) {

                        val order = orderList[holder.adapterPosition]
                        var branchAddress = ""
                        var branchImg = ""
                        var branchEmail = ""
                        var branchPhone = ""
                        var branchLat = 0.0
                        var branchLng = 0.0
                        var branchName = ""
                        db.collection("branches").document(order.branchId!!).get()
                            .addOnSuccessListener {
                                branchName = it.data!!["branch"].toString()
                                branchAddress = it.data!!["city"].toString()
                                branchImg = it.data!!["branchImg"].toString()
                                branchEmail = it.data!!["email"].toString()
                                branchPhone = it.data!!["phoneNo"].toString()
                                branchLat = it.data!!["latitude"].toString().toDouble()
                                branchLng = it.data!!["longitude"].toString().toDouble()
                                Glide.with(v.context).load(branchImg).into(holder.store)
                                holder.storeName.text = branchName




                            }.addOnFailureListener {

                            }

                        binding.earningsPlaceholder.visibility = View.GONE
                        binding.earningsCount.text = "My Deliveries (${orderList.size} orders)"


                        holder.orderId.text = order.orderNumber
                        val formatter = SimpleDateFormat("dd MMM yyyy HH:mm:ss")
                        val date: String = formatter.format(order.timestamp!!.toDate())
                        var completedOn: String? = null
                        if(order.deliveryDoneOn != null) {
                             completedOn =
                                formatter.format(order.deliveryDoneOn!!.toDate())
                            holder.orderTime.text = "Completed on $completedOn"
                        }
                        val startedOn : String = formatter.format(order.deliveryStartedOn!!.toDate())

                        holder.orderCommission.text = "+${order.deliveryCharge}"
                        holder.viewOrder.setOnClickListener {

                            val intent = Intent(it.context, EarningsDetailsActivity::class.java)
                            intent.putExtra("orderNumber", order.orderNumber)
                            intent.putExtra("orderId", order.id)
                            intent.putExtra("storeName", branchName)
                            intent.putExtra("storeId", order.branchId)
                            intent.putExtra("orderDate", date)
                            intent.putExtra("orderAmount", order.totalAmount)
                            intent.putExtra("branchAddress", branchAddress)
                            intent.putExtra("branchEmail", branchEmail)
                            intent.putExtra("branchPhone", branchPhone)
                            intent.putExtra("branchLat", branchLat)
                            intent.putExtra("branchLng", branchLng)
                            intent.putExtra("branchImg", branchImg)
                            intent.putExtra("orderDelCharge", Integer.valueOf(order.deliveryCharge.toString()))
                            intent.putExtra("custName",order.orderBy)
                            intent.putExtra("custPhone", order.orderByPhone)
                            intent.putExtra("deliveryAddress", order.deliveryAddress)
                            intent.putExtra("deliveryLat", order.deliveryLat)
                            intent.putExtra("deliveryLng", order.deliveryLng)
                            intent.putExtra("startedOn",startedOn)
                            intent.putExtra("completedOn",completedOn)

                            it.context.startActivity(intent)
                        }
                    }
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EarningsViewHolder {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.earnings_row, parent, false)
                    return EarningsViewHolder(view)
                }

                override fun getItemCount(): Int {
                    return orderList.size
                }


            }
            adapter!!.notifyDataSetChanged()
            binding.listEarnings.adapter = adapter
        } catch (e: Exception) {
            Log.e("RecycleAdapter", e.toString())
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        firestoreListener!!.remove()
    }

    override fun onStart() {
        super.onStart()


        if (adapter != null) {
            adapter!!.startListening()
        }


    }

    override fun onStop() {
        super.onStop()


        if (adapter != null) {
            adapter!!.stopListening()
        }


    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onResume() {
        super.onResume()
        orderList = ArrayList()
        val db = Firebase.firestore
        firestoreListener = db.collection("orders")
            .whereEqualTo("status", 6)
            .whereEqualTo("deliveryById", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener(EventListener { documentSnapshots, e ->
                if (e != null) {
                    Log.e("MainActivity", "Listen failed!", e)
                    return@EventListener
                }

                orderList = ArrayList()
                if (!documentSnapshots!!.isEmpty) {
                    for (doc in documentSnapshots) {
                        val order = doc.toObject(Order::class.java)
                        order.id = doc.id
                        orderList.add(order)

                    }
                }
                Log.e("MainActivity", "Listen success! $orderList")

            })
        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.listEarnings.layoutManager = mLayoutManager
        binding.listEarnings.itemAnimator = DefaultItemAnimator()
        adapter!!.notifyDataSetChanged()
        binding.listEarnings.adapter = adapter
    }

    companion object {

    }
}