package com.nazmar.musicgym

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Duration

class Converters {

    @TypeConverter
    fun listToString(list: List<String>): String = Gson().toJson(list)

    @TypeConverter
    fun stringToList(string: String): List<String> {
        return Gson().fromJson(string, object : TypeToken<List<String>>() {}.type)
    }

    @TypeConverter
    fun milliToDuration(millis: Long): Duration = Duration.ofMillis(millis)

    @TypeConverter
    fun durationToMilli(duration: Duration): Long = duration.toMillis()
}