package com.pango.pangodelivery.common

import android.content.Context
import android.content.SharedPreferences

class Common(val context: Context)  {
    val sharedPref = context.getSharedPreferences("PangoDelivery", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = sharedPref.edit()
    val myId = sharedPref.getString("uid", "1")
    val myEmail = sharedPref.getString("email", "1")
    val myPhone = sharedPref.getString("phone", "1")
    val myName = sharedPref.getString("name", "1")
    val myPic = sharedPref.getString("pic", "1")

}