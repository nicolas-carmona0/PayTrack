package com.nicolascarmona.paytrack.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nicolascarmona.paytrack.model.CategoryItem
import com.nicolascarmona.paytrack.model.PayTrackItem

// Une las tablas, el DAO y los conversores

@Database(entities = [PayTrackItem::class, CategoryItem::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PayTrackDatabase : RoomDatabase(){
    abstract val dao: SubscriptionDao
}