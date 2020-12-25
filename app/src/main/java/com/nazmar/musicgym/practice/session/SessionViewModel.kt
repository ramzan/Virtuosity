package com.nazmar.musicgym.practice.session

import android.app.Application
import android.text.Editable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.nazmar.musicgym.db.ExerciseDatabase
import com.nazmar.musicgym.db.HistoryItem
import com.nazmar.musicgym.db.Routine
import com.nazmar.musicgym.db.RoutineExercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessionViewModel(routineId: Long, application: Application) : AndroidViewModel(application) {

    private val dao = ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    val exercises = dao.getSessionExercises(routineId)

    val exercisesLoaded = Transformations.map(exercises) {
        !it.isNullOrEmpty()
    }

    private var newBpms = mutableListOf<Editable>()

    private var _currentIndex = MutableLiveData(-1)

    val currentIndex: LiveData<Int>
        get() = _currentIndex

    fun createBpmList(text: Editable) {
        if (newBpms.size == 0) {
            exercises.value?.forEach { _ ->
                newBpms.add(text)
            }
        }
    }

    fun nextExercise() {
        _currentIndex.value = _currentIndex.value!! + 1
    }

    fun previousExercise() {
        _currentIndex.value = _currentIndex.value!! - 1
    }

    fun getCurrentExerciseName(): String {
        return when (exercisesLoaded.value == true && currentIndex.value!! > -1) {
            true -> exercises.value!![currentIndex.value!!].name
            else -> ""
        }
    }

    fun getCurrentExerciseBpmRecord(): String {
        return when (exercisesLoaded.value == true && currentIndex.value!! > -1) {
            true -> exercises.value!![currentIndex.value!!].bpm.toString()
            else -> ""
        }
    }

    fun getNewExerciseBpm(): Editable? {
        return when (exercisesLoaded.value == true && currentIndex.value!! > -1) {
            true -> newBpms[currentIndex.value!!]
            else -> null
        }
    }

    fun nextButtonEnabled(): Boolean {
        return exercisesLoaded.value == true && currentIndex.value!! < exercises.value!!.size - 1
    }

    fun previousButtonEnabled(): Boolean {
        return exercisesLoaded.value == true && currentIndex.value!! > 0
    }

    fun updateBpm(bpm: Editable) {
        if (exercisesLoaded.value == true) {
            currentIndex.value?.let { newBpms[it] = bpm }
        }
    }

    fun saveSession() {
        CoroutineScope(Dispatchers.IO).launch {
            for (i in 0 until newBpms.size) {
                val bpm = newBpms[i].toString()
                if (bpm.isNotEmpty()) {
                    dao.insert(HistoryItem(exercises.value!![i].exerciseId,bpm.toInt() ))
                }
            }
        }
    }
}