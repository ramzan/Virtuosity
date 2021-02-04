package com.nazmar.musicgym.history


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.databinding.ListItemHistoryBinding
import com.nazmar.musicgym.db.SessionHistory
import java.util.*


class SessionHistoryAdapter(private val onDelete: (SessionHistory) -> Unit) :
    PagedListAdapter<SessionHistory, SessionHistoryAdapter.ViewHolder>(SessionHistoryDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: ListItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SessionHistory?, onDelete: (SessionHistory) -> Unit) {
            binding.apply {
                item?.run {
                    historyDeleteBtn.setOnClickListener { onDelete(item) }
                    historyTitle.text = title
                    historyDate.text = Date(time).toString()
                    historyData.text = buildSpannedString {
                        for (i in exercises.indices) {
                            append("${exercises[i]}: ${bpms[i]} BPM")
                            if (improvements[i].isEmpty()) append("\n")
                            else {
                                color(
                                    Color.GREEN
                                ) {
                                    this.append(" ${improvements[i]}\n")
                                }
                            }
                        }
                    }
                } ?: run {
                    historyTitle.text = ""
                    historyDate.text = ""
                    historyData.text = ""
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding =
                    ListItemHistoryBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class SessionHistoryDiffCallback : DiffUtil.ItemCallback<SessionHistory>() {
    override fun areItemsTheSame(oldItem: SessionHistory, newItem: SessionHistory): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SessionHistory, newItem: SessionHistory): Boolean {
        return oldItem == newItem
    }
}