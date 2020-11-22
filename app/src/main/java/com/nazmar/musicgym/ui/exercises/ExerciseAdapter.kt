package com.nazmar.musicgym.ui.exercises


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.databinding.ListItemExerciseBinding
import com.nazmar.musicgym.db.Exercise


class ExerciseAdapter(private val onClickListener: OnClickListener) :
        ListAdapter<Exercise, ExerciseAdapter.ViewHolder>(ExerciseDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(item)
        }
        holder.bind(item)
    }

    /**
     * Custom listener that handles clicks on [RecyclerView] items.  Passes the [Exercise]
     * associated with the current item to the [onClick] function.
     * @param clickListener lambda that will be called with the current [Exercise]
     */
    class OnClickListener(val clickListener: (lift: Exercise) -> Unit) {
        fun onClick(lift: Exercise) = clickListener(lift)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: ListItemExerciseBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Exercise) {
            binding.exerciseName.text = item.name
            binding.maxBpm.text = item.maxBpm.toString()
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

class ExerciseDiffCallback : DiffUtil.ItemCallback<Exercise>() {
    override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
        return oldItem.name == newItem.name && oldItem.maxBpm == newItem.maxBpm
    }

    override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
        return oldItem == newItem
    }
}
