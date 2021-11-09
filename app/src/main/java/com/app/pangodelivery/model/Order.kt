package com.app.pangodelivery.model

import java.io.Serializable

class Order : Serializable {
    var id: String? = null
    var currentStatus: String? = null
    var status: Int? = null
    var totalAmount: Int? = null
    var  timestamp: com.google.firebase.Timestamp? = null
    var uid: String? = null
    var paymentMode: Int? = null
    var paymentModeName: String? = null
    var orderType: Int? = null
    var orderTypeName: String? = null
    var orderNumber: String? = null
    var deliveryCharge: Long?= null
    var transactionFee: Long? = null
    var pangoCardNo: String? = null
    var fromApp:Int? = null
    var orderBy: String? = null
    var orderByName: String? = null
    var branchId: String? = null
    var storeId: String? = null
    var paymentStatus:Int? = null
    var branchName: String? = null
    var branchPhoneNo: String? = null
    var moreInfo: String? = null
    var orderByPhone: String? =null
    var deliveryAddress: String? =null
    var deliveryLat: String? =null
    var deliveryLng: String? =null
    var deliveryStartedOn: com.google.firebase.Timestamp? =null
    var deliveryByName: String? =null
    var deliveryByPhone: String? =null
    var deliveryByPhoto: String? =null
    var deliveryById: String? =null
    var deliveryDoneOn: com.google.firebase.Timestamp? = null

}