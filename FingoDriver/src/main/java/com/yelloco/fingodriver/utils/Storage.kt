package com.yelloco.fingodriver.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Storage
{
    private const val TAG = "Storage"
    private var sharedPreferences: SharedPreferences? = null

    fun initialize(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        }
    }

    fun storeString(key: String?, value: String?) {
        sharedPreferences?.let {
            val editor = it.edit()
            editor.putString(key, value)
            editor.apply()
        } ?: kotlin.run {
            Log.e(TAG, "Failed to storeString PREFS is NULL")
        }
    }

    fun storeInt(key: String?, value: Int) {
        sharedPreferences?.let {
            val editor = it.edit()
            editor.putInt(key, value)
            editor.apply()
        } ?: kotlin.run {
            Log.e(TAG, "Failed to storeInt PREFS is NULL")
        }
    }

    fun storeLong(key: String?, value: Long) {
        sharedPreferences?.let {
            val editor = it.edit()
            editor.putLong(key, value)
            editor.apply()
        } ?: kotlin.run {
            Log.e(TAG, "Failed to storeLong PREFS is NULL")
        }
    }

    fun storeBoolean(key: String?, value: Boolean) {
        sharedPreferences?.let {
            val editor = it.edit()
            editor.putBoolean(key, value)
            editor.apply()
        } ?: kotlin.run {
            Log.e(TAG, "Failed to storeBoolean PREFS is NULL")
        }
    }

    fun getString(key: String?): String? {
        return sharedPreferences?.getString(key, null) ?: kotlin.run {
            return null
        }
    }

    fun getLong(key: String?, defaultValue: Long): Long {
        return sharedPreferences?.getLong(key, defaultValue) ?: kotlin.run {
            return defaultValue
        }
    }

    fun getString(key: String?, defaultValue: String?): String? {
        return sharedPreferences?.getString(key, defaultValue) ?: kotlin.run {
            return defaultValue
        }
    }

    fun getInt(key: String?, defaultValue: Int): Int {
        return sharedPreferences?.getInt(key, defaultValue) ?: kotlin.run {
            Log.e(TAG, "Failed to getBoolean PREFS is NULL")
            return defaultValue
        }
    }

    fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
        return sharedPreferences?.getBoolean(key, defaultValue) ?: kotlin.run {
            Log.e(TAG, "Failed to getBoolean PREFS is NULL")
            return defaultValue
        }
    }

    operator fun contains(key: String): Boolean {
        return sharedPreferences?.contains(key) ?: kotlin.run {
            return false
        }
    }

    fun removeValue(key: String?) {
        sharedPreferences?.let {
            val editor = it.edit()
            editor.remove(key)
            editor.apply()
        } ?: kotlin.run {
            Log.e(TAG, "Failed to removeValue PREFS is NULL")
        }
    }

    fun <T> storeList(key: String?, list: List<T>?) {
        val gson = Gson()
        val s = gson.toJson(list)
        storeString(key, s)
    }

    fun <T> getList(key: String?, tt: TypeToken<List<T>?>): List<T> {
        val gson = Gson()
        val string = getString(key)
        return gson.fromJson(string, tt.type)
    }

    fun registerChangeListener(onSharedPreferenceChangeListener: OnSharedPreferenceChangeListener?) {
        sharedPreferences?.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
            ?: kotlin.run {
                Log.e(TAG, "Failed to registerChangeListener PREFS is NULL")
            }
    }

    fun unregisterChangeListener(onSharedPreferenceChangeListener: OnSharedPreferenceChangeListener?) {
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
            ?: kotlin.run {
            Log.e(TAG, "Failed to unregisterChangeListener PREFS is NULL")
        }
    }

    fun destroy() {
        sharedPreferences = null
    }
}