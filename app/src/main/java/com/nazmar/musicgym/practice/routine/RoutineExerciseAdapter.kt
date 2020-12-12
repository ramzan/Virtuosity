package com.nazmar.musicgym.practice.routine

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.databinding.ListItemRoutineExerciseBinding
import com.nazmar.musicgym.db.RoutineExerciseName


class RoutineExerciseAdapter :
        ListAdapter<RoutineExerciseName, RoutineExerciseAdapter.ViewHolder>(RoutineDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: ListItemRoutineExerciseBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RoutineExerciseName) {
            binding.exerciseName.text = item.name
            val duration = "${item.duration / 60}:${item.duration % 60}"
            binding.duration.text = duration
//            binding.order.text = item.order.toString()

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
    override fun areItemsTheSame(oldItem: RoutineExerciseName, newItem: RoutineExerciseName): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: RoutineExerciseName, newItem: RoutineExerciseName): Boolean {
        return oldItem == newItem
    }
}
