package ca.ramzan.virtuosity.screens.exercise_list


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.ramzan.virtuosity.databinding.ListItemExerciseBinding
import ca.ramzan.virtuosity.exercises.ExerciseLatestBpm
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import java.util.*


class ExerciseAdapter(private val onClickListener: (ExerciseLatestBpm, Int) -> Unit) :
    ListAdapter<ExerciseLatestBpm, ExerciseAdapter.ViewHolder>(ExerciseDiffCallback()),
    FastScrollRecyclerView.SectionedAdapter {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener(item, position)
        }
        holder.bind(item, currentSelected.contains(item))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: ListItemExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExerciseLatestBpm, selected: Boolean) {
            binding.exerciseName.text = item.name
            binding.maxBpm.text = item.bpmDisplay
            binding.root.isSelected = selected
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

    override fun getSectionName(position: Int): String {
        return currentList[position].name.first().toString().uppercase(Locale.ROOT)
    }

    fun submitSelected(newSelected: List<ExerciseLatestBpm>) {
        oldSelected = currentSelected
        currentSelected = newSelected
    }

    companion object {
        private var oldSelected: List<ExerciseLatestBpm> = emptyList()
        private var currentSelected: List<ExerciseLatestBpm> = emptyList()
        fun selectionChanged(exercise: ExerciseLatestBpm): Boolean {
            return oldSelected.contains(exercise) == currentSelected.contains(exercise)
        }

    }
}

class ExerciseDiffCallback : DiffUtil.ItemCallback<ExerciseLatestBpm>() {
    override fun areItemsTheSame(oldItem: ExerciseLatestBpm, newItem: ExerciseLatestBpm): Boolean {
        return oldItem.name == newItem.name && oldItem.bpm == newItem.bpm
    }

    override fun areContentsTheSame(
        oldItem: ExerciseLatestBpm,
        newItem: ExerciseLatestBpm
    ): Boolean {
        return oldItem == newItem && !ExerciseAdapter.selectionChanged(oldItem)
    }
}