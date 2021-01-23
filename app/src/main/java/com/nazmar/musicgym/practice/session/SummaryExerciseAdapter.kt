package com.nazmar.musicgym.practice.session

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.databinding.ListItemSummaryBinding
import kotlin.math.sign


class SummaryExerciseAdapter :
    ListAdapter<SummaryExercise, SummaryExerciseAdapter.ViewHolder>(SummaryExerciseDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: ListItemSummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SummaryExercise) {
            binding.apply {
                summaryItemName.text = item.name
                summaryItemBpm.text = item.newBpm.toString()
                summaryItemBpmDiff.text = (item.newBpm - item.oldBpm).let {
                    when (it.sign) {
                        1 -> "+$it"
                        else -> ""
                    }
                }
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

class SummaryExerciseDiffCallback : DiffUtil.ItemCallback<SummaryExercise>() {
    override fun areItemsTheSame(oldItem: SummaryExercise, newItem: SummaryExercise): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SummaryExercise, newItem: SummaryExercise): Boolean {
        return oldItem.newBpm == newItem.newBpm
    }
}

data class SummaryExercise(
    val id: Int,

    var name: String,

    val oldBpm: Int,

    var newBpm: Int
)