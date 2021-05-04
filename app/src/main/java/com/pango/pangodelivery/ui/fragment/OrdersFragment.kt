package com.pango.pangodelivery.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.common.internal.service.Common
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pango.pangodelivery.R
import com.pango.pangodelivery.databinding.FragmentOrdersBinding
import com.pango.pangodelivery.databinding.OrderRowBinding
import com.pango.pangodelivery.model.Order
import com.pango.pangodelivery.ui.OrderDetailsActivity
import com.pango.pangodelivery.viewholder.OrderViewHolder
import dmax.dialog.SpotsDialog
import es.dmoral.toasty.Toasty
import java.io.Serializable
import java.text.SimpleDateFormat


class OrdersFragment : Fragment() {
    private var firestoreListener: ListenerRegistration? = null
    private lateinit var orderList: ArrayList<Order>
    private var uid: String? = null
    private var adapter: FirestoreRecyclerAdapter<Order, OrderViewHolder>? = null
    private var _binding: FragmentOrdersBinding? = null

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
        // Inflate the layout for this fragment
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        val v = binding.root
        uid = requireArguments().getString("uid")
        val db = Firebase.firestore
        orderList = ArrayList()
        val mLayoutManager = LinearLayoutManager(v.context)
        binding.listOrders.layoutManager = mLayoutManager
        binding.listOrders.itemAnimator = DefaultItemAnimator()
        loadItemsList(db, v)
        firestoreListener = db.collection("orders")
            .whereEqualTo("status", 2)
            .whereEqualTo("orderType", 2)
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

        return v
    }

    private fun loadItemsList(db: FirebaseFirestore, v: View) {
        try {
            val query =
                db.collection("orders")
                    .whereEqualTo("status", 2)
                    .whereEqualTo("orderType", 2)
                    .orderBy("timestamp", Query.Direction.DESCENDING)

            val response = FirestoreRecyclerOptions.Builder<Order>()
                .setQuery(query, Order::class.java)
                .build()

            adapter = object : FirestoreRecyclerAdapter<Order, OrderViewHolder>(response) {

                override fun onBindViewHolder(
                    holder: OrderViewHolder,
                    position: Int,
                    model: Order
                ) {

                    if (orderList.size != 0) {

                        val order = orderList[holder.adapterPosition]
                        var branchAddress = ""
                        var branchImg = ""
                        var branchEmail = ""
                        var branchPhone = ""
                        var branchLat = 0.0f
                        var branchLng = 0.0f
                        var branchName = ""
                        db.collection("branches").document(order.branchId!!).get()
                            .addOnSuccessListener {
                                branchName = it.data!!["branch"].toString()
                                branchAddress = it.data!!["physicalAddr"].toString()
                                branchImg = it.data!!["branchImg"].toString()
                                branchEmail = it.data!!["email"].toString()
                                branchPhone = it.data!!["phoneNo"].toString()
                                branchLat = it.data!!["latitude"].toString().toFloat()
                                branchLng = it.data!!["longitude"].toString().toFloat()
                                Glide.with(v.context).load(branchImg).into(holder.store)
                            }.addOnFailureListener {

                            }

                        binding.orderPlaceholder.visibility = View.GONE
                        binding.ordersCount.text = "Available Orders (${orderList.size} orders)"
                        holder.storeName.text = branchName
                        holder.orderId.text = order.orderNumber
                        val formatter = SimpleDateFormat("dd MMM yyyy HH:mm:ss")
                        val date: String = formatter.format(order.timestamp!!.toDate())
                        holder.orderDate.text = date
                        holder.viewOrder.setOnClickListener {

                            val intent = Intent(it.context, OrderDetailsActivity::class.java)
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
                            intent.putExtra("orderDelCharge", order.deliveryCharge)
                            it.context.startActivity(intent)
                        }
                    }
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.order_row, parent, false)
                    return OrderViewHolder(view)
                }

                override fun getItemCount(): Int {
                    return orderList.size
                }


            }
            adapter!!.notifyDataSetChanged()
            binding.listOrders.adapter = adapter
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
            .whereEqualTo("status", 2)
            .whereEqualTo("orderType", 2)
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
        binding.listOrders.layoutManager = mLayoutManager
        binding.listOrders.itemAnimator = DefaultItemAnimator()
        adapter!!.notifyDataSetChanged()
        binding.listOrders.adapter = adapter
    }


    companion object {

    }
}