package com.nazmar.musicgym.exercises


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.databinding.ListItemExerciseBinding
import com.nazmar.musicgym.db.ExerciseMaxBpm


class ExerciseAdapter(private val onClickListener: OnClickListener) :
    ListAdapter<ExerciseMaxBpm, ExerciseAdapter.ViewHolder>(ExerciseDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(item)
        }
        holder.bind(item)
    }

    class OnClickListener(val clickListener: (lift: ExerciseMaxBpm) -> Unit) {
        fun onClick(lift: ExerciseMaxBpm) = clickListener(lift)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: ListItemExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExerciseMaxBpm) {
            binding.exerciseName.text = item.name
            binding.maxBpm.text = (item.bpm ?: 0).toString()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding =
                    ListItemExerciseBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class ExerciseDiffCallback : DiffUtil.ItemCallback<ExerciseMaxBpm>() {
    override fun areItemsTheSame(oldItem: ExerciseMaxBpm, newItem: ExerciseMaxBpm): Boolean {
        return oldItem.name == newItem.name && oldItem.bpm == newItem.bpm
    }

    override fun areContentsTheSame(oldItem: ExerciseMaxBpm, newItem: ExerciseMaxBpm): Boolean {
        return oldItem == newItem
    }
}