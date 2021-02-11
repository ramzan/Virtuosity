package com.nazmar.musicgym.common.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun listToString(list: List<String>): String = Gson().toJson(list)

    @TypeConverter
    fun stringToList(string: String): List<String> {
        return Gson().fromJson(string, object : TypeToken<List<String>>() {}.type)
    }
}