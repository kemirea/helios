package com.kemikalreaktion.helios

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Paper::class], version = 1)
abstract class PaperDatabase : RoomDatabase() {

    abstract fun paperDao(): PaperDao

    companion object {
        @Volatile
        private var INSTANCE: PaperDatabase? = null

        fun getDatabase(context: Context) : PaperDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PaperDatabase::class.java,
                    "paper_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }
}