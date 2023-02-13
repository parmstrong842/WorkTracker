package com.example.worktracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Shift::class], version = 4, exportSchema = false)
abstract class WorkTrackerDatabase : RoomDatabase() {

    abstract fun shiftDao(): ShiftDao

    companion object {
        @Volatile
        private var Instance: WorkTrackerDatabase? = null

        fun getDatabase(context: Context): WorkTrackerDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, WorkTrackerDatabase::class.java, "shift_database")
                    /**
                     * Setting this option in your app's database builder means that Room
                     * permanently deletes all data from the tables in your database when it
                     * attempts to perform a migration with no defined migration path.
                     */
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}