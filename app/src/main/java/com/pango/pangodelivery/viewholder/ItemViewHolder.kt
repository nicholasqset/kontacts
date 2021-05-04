package com.pango.pangodelivery.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pango.pangodelivery.R
import de.hdodenhof.circleimageview.CircleImageView

class ItemViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    var title: TextView = view.findViewById(R.id.itemName)
    var price: TextView  = view.findViewById(R.id.itemPrice)
    var quant: TextView  = view.findViewById(R.id.itemQuant)
    var image: CircleImageView = view.findViewById(R.id.itemPic)



}