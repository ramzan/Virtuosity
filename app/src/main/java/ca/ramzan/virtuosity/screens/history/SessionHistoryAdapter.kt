package ca.ramzan.virtuosity.screens.history


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ca.ramzan.virtuosity.databinding.ListItemHistoryBinding
import ca.ramzan.virtuosity.history.SessionHistory


class SessionHistoryDisplayAdapter(private val onDelete: (Long) -> Unit) :
    PagingDataAdapter<SessionHistory, SessionHistoryDisplayAdapter.HistoryViewHolder>(
        SessionHistoryDisplayDiffCallback()
    ) {

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position), onDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder.from(parent)
    }

    class HistoryViewHolder private constructor(private val binding: ListItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SessionHistory?, onDelete: (Long) -> Unit) {
            binding.apply {
                item?.run {
                    historyDeleteBtn.setOnClickListener {
                        onDelete(item.id)
                    }
                    historyTitle.text = title
                    historyDate.text = time
                    historyData.text = text
                } ?: run {
                    historyTitle.text = ""
                    historyDate.text = ""
                    historyData.text = ""
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): HistoryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding =
                    ListItemHistoryBinding.inflate(layoutInflater, parent, false)

                return HistoryViewHolder(binding)
            }
        }
    }
}

class SessionHistoryDisplayDiffCallback : DiffUtil.ItemCallback<SessionHistory>() {
    override fun areItemsTheSame(
        oldItem: SessionHistory,
        newItem: SessionHistory
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: SessionHistory,
        newItem: SessionHistory
    ): Boolean {
        return oldItem == newItem
    }
}