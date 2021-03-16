package ca.ramzan.virtuosity.screens.exerciselist


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.ramzan.virtuosity.databinding.ListItemExerciseBinding
import ca.ramzan.virtuosity.exercises.ExerciseMaxBpm
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import java.util.*


class ExerciseAdapter(private val onClickListener: (ExerciseMaxBpm) -> Unit) :
    ListAdapter<ExerciseMaxBpm, ExerciseAdapter.ViewHolder>(ExerciseDiffCallback()),
    FastScrollRecyclerView.SectionedAdapter {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener(item)
        }
        holder.bind(item)
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

    override fun getSectionName(position: Int): String {
        return currentList[position].name.first().toString().toUpperCase(Locale.ROOT)
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