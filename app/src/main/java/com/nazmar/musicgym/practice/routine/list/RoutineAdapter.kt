package com.nazmar.musicgym.practice.routine.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.databinding.ListItemRoutineBinding
import com.nazmar.musicgym.db.Routine


class RoutineAdapter(private val onEditListener: OnClickListener, private val onStartListener: OnClickListener) :
        ListAdapter<Routine, RoutineAdapter.ViewHolder>(RoutineDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onEditListener, onStartListener)
    }

    class OnClickListener(val clickListener: (routine: Routine) -> Unit) {
        fun onClick(routine: Routine) = clickListener(routine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: ListItemRoutineBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Routine, onEditListener: OnClickListener, onStartListener: OnClickListener) {
            binding.routineTitle.text = item.name
            binding.editRoutineButton.setOnClickListener {
                onEditListener.onClick(item)
            }
            binding.startRoutineButton.setOnClickListener {
                onStartListener.onClick(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding =
                        ListItemRoutineBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class RoutineDiffCallback : DiffUtil.ItemCallback<Routine>() {
    override fun areItemsTheSame(oldItem: Routine, newItem: Routine): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Routine, newItem: Routine): Boolean {
        return oldItem == newItem
    }
}
