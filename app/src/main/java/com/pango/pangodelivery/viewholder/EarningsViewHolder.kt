package com.pango.pangodelivery.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pango.pangodelivery.R
import de.hdodenhof.circleimageview.CircleImageView

class EarningsViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    var orderId: TextView = view.findViewById(R.id.orderId)
    var viewOrder: TextView = view.findViewById(R.id.viewOrder)
    var orderCommission: TextView = view.findViewById(R.id.orderCommission)
    var orderTime: TextView = view.findViewById(R.id.orderTime)
    var storeName: TextView = view.findViewById(R.id.storeName)
    var store: CircleImageView = view.findViewById(R.id.storePic)
}