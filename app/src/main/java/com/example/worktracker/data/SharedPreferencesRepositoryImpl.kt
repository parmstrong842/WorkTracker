package com.example.worktracker.data

import android.content.SharedPreferences

class SharedPreferencesRepositoryImpl(private val sharedPreferences: SharedPreferences) : SharedPreferencesRepository {
    override fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue)!!
    }

    override fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    override fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    override fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
}