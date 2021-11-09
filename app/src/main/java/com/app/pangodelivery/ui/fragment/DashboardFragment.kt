package com.app.pangodelivery.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import com.app.pangodelivery.databinding.FragmentDashboardBinding

import com.app.pangodelivery.model.Order



class DashboardFragment : Fragment() {

    private var firestoreListener: ListenerRegistration? = null
    private lateinit var orderList: ArrayList<Order>
    private var uid: String? = null
    private var earnings: Int = 0
    private var _binding: FragmentDashboardBinding? = null

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
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val v = binding.root
        uid = requireArguments().getString("uid")
        val db = Firebase.firestore
        orderList = ArrayList()
        db.collection("orders")
            .whereEqualTo("status", 6)
            .whereEqualTo("deliveryById", uid)
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
                        earnings += doc.data["deliveryCharge"].toString().toInt()

                    }
                    binding.totalOrders.text = orderList.size.toString()
                    binding.totalEarnings.text = earnings.toString()
                    binding.totalNet.text = earnings.toString()
                }
                Log.e("MainActivity", "Listen success! $orderList")

            })
        return v
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

    }
}