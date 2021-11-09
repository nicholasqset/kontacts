package com.app.pangodelivery.model

import java.io.Serializable

class Item : Serializable {
    var id: String? = null
    var item: String? = null
    var itemId: String? = null
    var price: String? = null
    var quantity: Int? = null
    var itemImage: String? = null
    var itemUnit: String? = null
    var storeId: String? = null
    var branchId: String? = null
    var itemPrepTime: String? = null
    var itemTransCharge: String? = null
    var itemDiscount: String? = null
}