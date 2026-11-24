package com.example.turnthemiddleage

import android.content.Context
import android.content.SharedPreferences


class levelhandler(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences("level", Context.MODE_PRIVATE)

    fun saveLevel(level: Int) {
        preferences.edit().putInt("level", level).apply()
    }

    fun loadLevel(level: Int? = null): Int {
        val savedLevel = preferences.getInt("level", 0)

        return if (level == null && savedLevel != 0) {
            savedLevel
        } else {
            level ?: 1
        }
    }
}
