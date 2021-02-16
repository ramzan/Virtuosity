package com.nazmar.musicgym.screens.routineeditor

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.underline
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.common.millisToTimerString
import com.nazmar.musicgym.databinding.ListItemRoutineExerciseBinding
import com.nazmar.musicgym.routine.RoutineExercise


class RoutineExerciseAdapter(
    private val fragment: RoutineEditorFragment,
    private val onClickListener: (exerciseIndex: Int, duration: Long) -> Unit
) : ListAdapter<RoutineExercise, RoutineExerciseAdapter.ViewHolder>(RoutineDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClickListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder.from(parent).also {
            it.dragHandle.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    fragment.startDragging(it)
                }
                true
            }
        }
    }

    class ViewHolder private constructor(private val binding: ListItemRoutineExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val dragHandle = binding.dragHandle

        fun bind(
            item: RoutineExercise,
            onClickListener: (exerciseIndex: Int, duration: Long) -> Unit
        ) {
            binding.apply {
                exerciseName.text = item.name
                duration.text = buildSpannedString {
                    underline { append(millisToTimerString(item.duration)) }
                }
                duration.setOnClickListener {
                    onClickListener(bindingAdapterPosition, item.duration)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding =
                    ListItemRoutineExerciseBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class RoutineDiffCallback : DiffUtil.ItemCallback<RoutineExercise>() {
    override fun areItemsTheSame(
        oldItem: RoutineExercise,
        newItem: RoutineExercise
    ): Boolean {
        return oldItem.name == newItem.name && oldItem.duration == newItem.duration
    }

    override fun areContentsTheSame(
        oldItem: RoutineExercise,
        newItem: RoutineExercise
    ): Boolean {
        return oldItem == newItem
    }
}
