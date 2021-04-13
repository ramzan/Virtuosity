package ca.ramzan.virtuosity.screens.history


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ca.ramzan.virtuosity.databinding.ListItemHistoryOuterBinding
import ca.ramzan.virtuosity.history.SessionHistoryEntity


class HistoryOuterAdapter(private val onDelete: (Long) -> Unit) :
    PagingDataAdapter<SessionHistoryEntity, HistoryOuterAdapter.HistoryViewHolder>(
        HistoryOuterDiffCallback()
    ) {

    private val sharedPool = RecyclerView.RecycledViewPool()

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position), onDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder.from(parent, sharedPool)
    }

    class HistoryViewHolder private constructor(private val binding: ListItemHistoryOuterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SessionHistoryEntity?, onDelete: (Long) -> Unit) {
            binding.apply {
                item?.run {
                    historyDeleteBtn.setOnClickListener {
                        onDelete(item.id)
                    }
                    historyTitle.text = title
                    historyDate.text = displayTime
                    historyData.adapter = HistoryInnerAdapter(exercises, bpms, improvements)
                    if (note == null) {
                        notesData.visibility = View.GONE
                        headerNotes.visibility = View.GONE
                    } else {
                        notesData.text = note
                        headerNotes.visibility = View.VISIBLE
                        notesData.visibility = View.VISIBLE
                    }
                } ?: run {
                    historyTitle.text = ""
                    historyDate.text = ""
                    historyData.adapter = null
                    notesData.text = ""
                }
            }
        }

        companion object {
            fun from(
                parent: ViewGroup,
                sharedPool: RecyclerView.RecycledViewPool
            ): HistoryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding =
                    ListItemHistoryOuterBinding.inflate(layoutInflater, parent, false)

                binding.historyData.apply {
                    setRecycledViewPool(sharedPool)
                }

                return HistoryViewHolder(binding)
            }
        }
    }
}

class HistoryOuterDiffCallback : DiffUtil.ItemCallback<SessionHistoryEntity>() {
    override fun areItemsTheSame(
        oldItem: SessionHistoryEntity,
        newItem: SessionHistoryEntity
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: SessionHistoryEntity,
        newItem: SessionHistoryEntity
    ): Boolean {
        return oldItem == newItem
    }
}