package ca.ramzan.virtuosity.screens.routineeditor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.underline
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.ramzan.virtuosity.common.millisToTimerString
import ca.ramzan.virtuosity.databinding.ListItemRoutineExerciseBinding
import ca.ramzan.virtuosity.routine.RoutineExercise


class RoutineExerciseAdapter(
    private val onDurationClick: (exerciseIndex: Int, duration: Long) -> Unit,
    private val onDeleteClick: (exerciseIndex: Int) -> Unit
) : ListAdapter<RoutineExercise, RoutineExerciseAdapter.ViewHolder>(RoutineDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onDurationClick, onDeleteClick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: ListItemRoutineExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: RoutineExercise,
            onDurationClick: (exerciseIndex: Int, duration: Long) -> Unit,
            onDeleteClick: (exerciseIndex: Int) -> Unit
        ) {
            binding.apply {
                exerciseName.text = item.name
                duration.text = buildSpannedString {
                    underline { append(millisToTimerString(item.duration)) }
                }
                duration.setOnClickListener {
                    onDurationClick(bindingAdapterPosition, item.duration)
                }
                deleteBtn.setOnClickListener {
                    onDeleteClick(bindingAdapterPosition)
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
