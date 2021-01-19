package com.nazmar.musicgym.practice.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.nazmar.musicgym.db.ExerciseDatabase
import com.nazmar.musicgym.db.HistoryItem
import com.nazmar.musicgym.updateBpm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessionViewModel(routineId: Long, application: Application) : AndroidViewModel(application) {

    private val dao = ExerciseDatabase.getInstance(application).exerciseDatabaseDao

    val session = dao.getRoutine(routineId)

    val exercises = dao.getSessionExercises(routineId)

    private var newBpms = mutableListOf<String>()

    private var _summaryList = MutableLiveData(mutableListOf<SummaryExercise>())

    val summaryList = Transformations.map(_summaryList) {
        it.filter { e -> e.newBpm != 0 }
    }

    private var _currentIndex = MutableLiveData(-1)

    val currentIndex: LiveData<Int>
        get() = _currentIndex

    fun createBpmList() {
        if (newBpms.size == 0) {
            var i = 0
            exercises.value?.forEach { e ->
                newBpms.add("")
                _summaryList.value?.add(SummaryExercise(i++, e.name, e.bpm, 0))
            }
            newBpms.add("")
        }
    }

    fun nextExercise() {
        _currentIndex.value = _currentIndex.value!! + 1
    }

    fun previousExercise() {
        _currentIndex.value = _currentIndex.value!! - 1
    }

    val currentExercise = Transformations.map(currentIndex) {
        exercises.value?.let { exercises ->
            when (it) {
                exercises.size -> null
                -1 -> null
                else -> exercises[it]
            }
        }
    }

    val currentExerciseName: String
        get() = currentExercise.value?.name ?: ""

    val currentExerciseBpmRecord: String
        get() = (currentExercise.value?.bpm ?: 0).toString()

    val newExerciseBpm: String
        get() = newBpms[currentIndex.value!!]

    val nextButtonEnabled: Boolean
        get() = currentIndex.value!! > -1 && currentIndex.value!! < exercises.value!!.size

    val previousButtonEnabled: Boolean
        get() = currentIndex.value!! > 0

    fun updateBpm(bpm: String) {
        currentIndex.value?.let {
            newBpms[it] = bpm
            _summaryList.updateBpm(it, bpm)
        }
    }

    fun saveSession() {
        CoroutineScope(Dispatchers.IO).launch {
            for (i in 0 until newBpms.size) {
                val bpm = newBpms[i]
                if (bpm.isNotEmpty()) {
                    dao.insert(HistoryItem(exercises.value!![i].exerciseId, bpm.toInt()))
                }
            }
        }
    }

    // Timer editor

    private var _editorTime = MutableLiveData<Long?>(null)

    val editorTime: LiveData<Long?>
        get() = _editorTime

    fun updateEditorTime(time: Long) {
        _editorTime.value = time
    }

    fun clearEditorTime() {
        _editorTime.value = null
    }
}