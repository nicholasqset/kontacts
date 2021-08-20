package com.pango.pangodelivery.ui.fragment

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pango.pangodelivery.R
import com.pango.pangodelivery.databinding.FragmentOrdersBinding
import com.pango.pangodelivery.model.Order
import com.pango.pangodelivery.ui.OrderDetailsActivity
import com.pango.pangodelivery.viewholder.OrderViewHolder
import java.text.SimpleDateFormat
import kotlin.math.roundToInt


class OrdersFragment(uid: String, myLat: Double?, myLng: Double?) : Fragment() {
    private var firestoreListener: ListenerRegistration? = null
    private lateinit var orderList: ArrayList<Order>

    private var adapter: FirestoreRecyclerAdapter<Order, OrderViewHolder>? = null
    private var _binding: FragmentOrdersBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var driverLatLng: LatLng? = null

    val uid = uid
    val myLat = myLat
    val myLng = myLng

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
       /* uid = requireArguments().getString("uid")
        val myLat = requireArguments().getDouble("myLat", 0.0)
        val myLng = requireArguments().getDouble("myLng",0.0)*/
        driverLatLng = LatLng(myLat!!,myLng!!)
        Log.e("OrdersFragment", "driverLatLng! $driverLatLng")

        val db = Firebase.firestore
        orderList = ArrayList()
        val mLayoutManager = LinearLayoutManager(v.context)
        binding.listOrders.layoutManager = mLayoutManager
        binding.listOrders.itemAnimator = DefaultItemAnimator()
        loadItemsList(db, v)
        firestoreListener = db.collection("orders")
            .whereEqualTo("status", 3)
            .whereEqualTo("currentStatus", "Delivery")
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
                    .whereEqualTo("status", 3)
                    .whereEqualTo("orderType", 2)
                    .whereEqualTo("currentStatus", "Delivery")
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
                        var branchLat = 0.0
                        var branchLng = 0.0
                        var branchName = ""
                        var distance = 0
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
                                val crntLocation = Location("crntlocation")
                                crntLocation.latitude = driverLatLng!!.latitude
                                crntLocation.longitude = driverLatLng!!.longitude

                                val newLocation = Location("newlocation")
                                newLocation.latitude = branchLat
                                newLocation.longitude = branchLng


                                //float distance = crntLocation.distanceTo(newLocation);  in meters
                                distance =(crntLocation.distanceTo(newLocation) / 1000).roundToInt() // in km
                                Log.e("OrdersFrag","distanceTo "+ distance)
                                holder.orderDistance.text = "~${distance}Kms"
                            }.addOnFailureListener {

                            }

                        binding.orderPlaceholder.visibility = View.GONE
                        binding.ordersCount.text = "Unassigned orders (${orderList.size} orders)"


                        holder.orderId.text = order.orderNumber
                        val formatter = SimpleDateFormat("dd MMM yyyy HH:mm:ss")
                        val date: String = formatter.format(order.timestamp!!.toDate())
                        holder.orderDate.text = date

                        holder.orderTime.visibility = View.GONE
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
                            intent.putExtra("custName",order.orderBy)
                            intent.putExtra("custPhone", order.orderByPhone)
                            intent.putExtra("deliveryAddress", order.deliveryAddress)
                            intent.putExtra("deliveryLat", order.deliveryLat)
                            intent.putExtra("deliveryLng", order.deliveryLng)
                            intent.putExtra("distance",distance)

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
            .whereEqualTo("status", 3)
            .whereEqualTo("orderType", 2)
            .whereEqualTo("currentStatus", "Delivery")
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