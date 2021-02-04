package com.nazmar.musicgym.history


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.databinding.ListItemHistoryBinding
import com.nazmar.musicgym.db.SessionHistoryDisplay
import com.nazmar.musicgym.setSafeOnClickListener
import java.util.*


class SessionHistoryDisplayAdapter(private val onDelete: (Long) -> Unit) :
    PagedListAdapter<SessionHistoryDisplay, SessionHistoryDisplayAdapter.ViewHolder>(
        SessionHistoryDisplayDiffCallback()
    ) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: ListItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SessionHistoryDisplay?, onDelete: (Long) -> Unit) {
            binding.apply {
                item?.run {
                    historyDeleteBtn.setSafeOnClickListener {
                        onDelete(item.id)
                    }
                    historyTitle.text = title
                    historyDate.text = Date(time).toString()
                    historyData.text = text
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

class SessionHistoryDisplayDiffCallback : DiffUtil.ItemCallback<SessionHistoryDisplay>() {
    override fun areItemsTheSame(
        oldItem: SessionHistoryDisplay,
        newItem: SessionHistoryDisplay
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: SessionHistoryDisplay,
        newItem: SessionHistoryDisplay
    ): Boolean {
        return oldItem == newItem
    }
}