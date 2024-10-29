package com.example.worktracker.data

interface SharedPreferencesRepository {
    fun getString(key: String, defaultValue: String): String

    fun putString(key: String, value: String)

    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    fun putBoolean(key: String, value: Boolean)

    fun remove(key: String)

    fun contains(key: String): Boolean
}