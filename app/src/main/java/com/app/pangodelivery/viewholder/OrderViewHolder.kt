package com.app.pangodelivery.viewholder

import android.view.View
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.app.pangodelivery.R
import de.hdodenhof.circleimageview.CircleImageView


class OrderViewHolder (view: View) : RecyclerView.ViewHolder(view) {

    var orderId: TextView = view.findViewById(R.id.orderId)
    var orderDate: TextView = view.findViewById(R.id.orderDate)
    var viewOrder: TextView = view.findViewById(R.id.viewOrder)
    var orderDistance: TextView = view.findViewById(R.id.orderDistance)
    var orderTime: TextView = view.findViewById(R.id.orderTime)
    var storeName: TextView = view.findViewById(R.id.storeName)
    var store: CircleImageView = view.findViewById(R.id.storePic)

}