package com.fb.roottest.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey
    @ColumnInfo(name = "purchase") val purchase: String,
    @ColumnInfo(name = "count") val count: Int,
    @ColumnInfo(name = "cost") val cost: Int
) {


}