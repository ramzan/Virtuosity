package com.nazmar.musicgym.screens.routinelist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nazmar.musicgym.databinding.ListItemRoutineBinding
import com.nazmar.musicgym.databinding.SavedSessionCardBinding
import java.time.Instant

private const val ITEM_VIEW_TYPE_SESSION = 0
private const val ITEM_VIEW_TYPE_ROUTINE = 1

class RoutineListCardAdapter(private val onClickListener: OnClickListener) :
    ListAdapter<RoutineListCard, RecyclerView.ViewHolder>(RoutineListCardDiffCallback()) {

    interface OnClickListener {
        fun onEdit(routine: RoutineListCard.RoutineCard)
        fun onStart(routine: RoutineListCard.RoutineCard)
        fun onResumeSession()
    }

    class SavedSessionCardViewHolder(private val binding: SavedSessionCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RoutineListCard.SavedSessionCard, onClickListener: OnClickListener) {
            binding.apply {
                savedSessionName.text = item.name
                savedSessionDate.text = item.time.toString()
                resumeSessionBtn.setOnClickListener {
                    onClickListener.onResumeSession()
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): SavedSessionCardViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SavedSessionCardBinding.inflate(layoutInflater, parent, false)
                return SavedSessionCardViewHolder(binding)
            }
        }
    }

    class RoutineCardViewHolder private constructor(private val binding: ListItemRoutineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RoutineListCard.RoutineCard, onClickListener: OnClickListener) {
            binding.routineTitle.text = item.name
            binding.editRoutineButton.setOnClickListener {
                onClickListener.onEdit(item)
            }
            binding.startRoutineButton.setOnClickListener {
                onClickListener.onStart(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): RoutineCardViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding =
                    ListItemRoutineBinding.inflate(layoutInflater, parent, false)

                return RoutineCardViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is RoutineCardViewHolder -> holder.bind(
                item as RoutineListCard.RoutineCard,
                onClickListener
            )
            is SavedSessionCardViewHolder -> holder.bind(
                item as RoutineListCard.SavedSessionCard,
                onClickListener
            )
            else -> throw Exception("Illegal holder type: $holder")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_ROUTINE -> RoutineCardViewHolder.from(parent)
            ITEM_VIEW_TYPE_SESSION -> SavedSessionCardViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is RoutineListCard.RoutineCard -> ITEM_VIEW_TYPE_ROUTINE
            is RoutineListCard.SavedSessionCard -> ITEM_VIEW_TYPE_SESSION
        }
    }

    fun addSavedSessionCardAndSubmitList(
        list: List<RoutineListCard>,
        sessionName: String,
        sessionTime: Long
    ) {
        submitList(
            listOf(
                RoutineListCard.SavedSessionCard(sessionName, Instant.ofEpochMilli(sessionTime))
            ) + list
        )
    }
}

class RoutineListCardDiffCallback : DiffUtil.ItemCallback<RoutineListCard>() {
    override fun areItemsTheSame(oldItem: RoutineListCard, newItem: RoutineListCard): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RoutineListCard, newItem: RoutineListCard): Boolean {
        return oldItem == newItem
    }
}

sealed class RoutineListCard {
    abstract val id: Long

    data class SavedSessionCard(
        val name: String,
        val time: Instant,
        override val id: Long = -1
    ) : RoutineListCard()

    data class RoutineCard(
        override val id: Long,
        val name: String
    ) : RoutineListCard()
}