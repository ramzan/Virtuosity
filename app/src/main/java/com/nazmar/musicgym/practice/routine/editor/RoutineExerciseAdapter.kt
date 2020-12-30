package com.nazmar.musicgym.practice.routine.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.databinding.ListItemRoutineExerciseBinding
import com.nazmar.musicgym.db.RoutineExerciseName


class RoutineExerciseAdapter(private val onClickListener: (exerciseIndex: Int, duration: Long) -> Unit) :
        ListAdapter<RoutineExerciseName, RoutineExerciseAdapter.ViewHolder>(RoutineDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(private val binding: ListItemRoutineExerciseBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RoutineExerciseName, onClickListener: (exerciseIndex: Int, duration: Long) -> Unit) {
            binding.exerciseName.text = item.name
            val duration = "${item.minutes}:${item.seconds.toString().padStart(2, '0')}"
            binding.duration.text = duration
            binding.duration.setOnClickListener {
                onClickListener(bindingAdapterPosition, item.getDuration())
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

class RoutineDiffCallback : DiffUtil.ItemCallback<RoutineExerciseName>() {
    override fun areItemsTheSame(
            oldItem: RoutineExerciseName,
            newItem: RoutineExerciseName
    ): Boolean {
        return oldItem.name == newItem.name && oldItem.getDuration() == newItem.getDuration()
    }

    override fun areContentsTheSame(
            oldItem: RoutineExerciseName,
            newItem: RoutineExerciseName
    ): Boolean {
        return oldItem == newItem
    }
}