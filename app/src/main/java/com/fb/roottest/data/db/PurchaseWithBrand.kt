package com.fb.roottest.data.db

import androidx.room.Embedded
import androidx.room.Relation

class PurchaseWithBrand {
    @Embedded
    lateinit var purchase: Purchase
    @Relation(parentColumn = "id", entityColumn = "brand_id")
    lateinit var brandsList: List<Brand>
}