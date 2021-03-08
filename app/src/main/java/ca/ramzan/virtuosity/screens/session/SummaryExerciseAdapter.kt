package ca.ramzan.virtuosity.screens.session

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.ramzan.virtuosity.databinding.ListItemSummaryBinding
import ca.ramzan.virtuosity.session.SessionExercise


class SummaryExerciseAdapter :
    ListAdapter<SessionExercise, SummaryExerciseAdapter.ViewHolder>(SessionExerciseDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: ListItemSummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SessionExercise) {
            binding.apply {
                summaryItemName.text = item.name
                summaryItemBpm.text = item.newBpm
                summaryItemBpmDiff.text = item.getBpmDiff()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding =
                    ListItemSummaryBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class SessionExerciseDiffCallback : DiffUtil.ItemCallback<SessionExercise>() {
    override fun areItemsTheSame(oldItem: SessionExercise, newItem: SessionExercise): Boolean {
        return oldItem.order == newItem.order
    }

    override fun areContentsTheSame(oldItem: SessionExercise, newItem: SessionExercise): Boolean {
        return oldItem.newBpm == newItem.newBpm
    }
}