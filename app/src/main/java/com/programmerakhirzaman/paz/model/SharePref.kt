package com.programmerakhirzaman.paz.model

import android.content.Context

class SharePref (context: Context) {
    val PRIVATE_MODE = 0

    private val PREF_NAME = "share_pref"
    private val sharedPref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
    private val editor = sharedPref.edit()

    fun setSessionString(key: String, value: String) {
        editor.putString(key, value)
        editor.commit()
    }

    fun setSessionNIK(key: String, value: String){
        editor.putString(key, value)
        editor.commit()
    }

    fun getSessionNik(key: String): String?{
        return sharedPref.getString(key, null)
    }

    fun getSessionString(key: String): String?{
        return sharedPref.getString(key, null)
    }

    companion object {
        const val key_level ="key_level"
    }

    fun removeData() {
        editor.clear()
        editor.apply()
    }
}