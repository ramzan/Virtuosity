package com.nazmar.musicgym.practice.routine.editor

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.underline
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.databinding.ListItemRoutineExerciseBinding
import com.nazmar.musicgym.db.RoutineExerciseName
import java.text.SimpleDateFormat
import java.util.*


class RoutineExerciseAdapter(
    private val fragment: RoutineEditorFragment,
    private val onClickListener: (exerciseIndex: Int, duration: Long) -> Unit
) : ListAdapter<RoutineExerciseName, RoutineExerciseAdapter.ViewHolder>(RoutineDiffCallback()) {

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
            item: RoutineExerciseName,
            onClickListener: (exerciseIndex: Int, duration: Long) -> Unit
        ) {
            binding.apply {
                exerciseName.text = item.name
                duration.text = buildSpannedString {
                    underline { append(timeFormatter.format(item.duration)) }
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

            val timeFormatter = SimpleDateFormat("mm:ss", Locale.US)
        }
    }
}

class RoutineDiffCallback : DiffUtil.ItemCallback<RoutineExerciseName>() {
    override fun areItemsTheSame(
        oldItem: RoutineExerciseName,
        newItem: RoutineExerciseName
    ): Boolean {
        return oldItem.name == newItem.name && oldItem.duration == newItem.duration
    }

    override fun areContentsTheSame(
        oldItem: RoutineExerciseName,
        newItem: RoutineExerciseName
    ): Boolean {
        return oldItem == newItem
    }
}
