package ca.ramzan.virtuosity.screens.routine_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.ramzan.virtuosity.common.DateFormatter
import ca.ramzan.virtuosity.databinding.ListItemNoRoutinesBinding
import ca.ramzan.virtuosity.databinding.ListItemRoutineBinding
import ca.ramzan.virtuosity.databinding.ListItemRoutineHeaderBinding
import ca.ramzan.virtuosity.databinding.SavedSessionCardBinding
import java.time.Instant

private const val ITEM_VIEW_TYPE_SESSION = 0
private const val ITEM_VIEW_TYPE_ROUTINE = 1
private const val ITEM_VIEW_TYPE_ROUTINE_HEADER = 2
private const val ITEM_VIEW_TYPE_NO_ROUTINES = 3

class RoutineListCardAdapter(private val onClickListener: OnClickListener) :
    ListAdapter<RoutineListCard, RecyclerView.ViewHolder>(RoutineListCardDiffCallback()) {

    interface OnClickListener {
        fun onCreateRoutine()
        fun onEditRoutine(routine: RoutineListCard.RoutineCard)
        fun onStartSession(routine: RoutineListCard.RoutineCard)
        fun onResumeSession()
        fun onCancelSession()
    }

    class SavedSessionCardViewHolder(private val binding: SavedSessionCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RoutineListCard.SavedSessionCard, onClickListener: OnClickListener) {
            binding.apply {
                savedSessionName.text = item.name
                savedSessionDate.text = DateFormatter.fromInstant(item.time)
                resumeSessionBtn.setOnClickListener {
                    onClickListener.onResumeSession()
                }
                cancelSessionBtn.setOnClickListener {
                    onClickListener.onCancelSession()
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
            binding.routinePreview.text = item.preview
            binding.editRoutineButton.setOnClickListener {
                onClickListener.onEditRoutine(item)
            }
            binding.startRoutineButton.setOnClickListener {
                onClickListener.onStartSession(item)
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

    class RoutineHeaderViewHolder private constructor(binding: ListItemRoutineHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val newRoutineBtn = binding.newRoutineBtn

        companion object {
            fun from(parent: ViewGroup): RoutineHeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding =
                    ListItemRoutineHeaderBinding.inflate(layoutInflater, parent, false)

                return RoutineHeaderViewHolder(binding)
            }
        }
    }

    class NoRoutinesMessageViewHolder private constructor(binding: ListItemNoRoutinesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): NoRoutinesMessageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding =
                    ListItemNoRoutinesBinding.inflate(layoutInflater, parent, false)

                return NoRoutinesMessageViewHolder(binding)
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
            is RoutineHeaderViewHolder -> {
                holder.newRoutineBtn.setOnClickListener {
                    onClickListener.onCreateRoutine()
                }
            }

            is NoRoutinesMessageViewHolder -> {
                /* no-op */
            }
            else -> throw Exception("Illegal holder type: $holder")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_ROUTINE -> RoutineCardViewHolder.from(parent)
            ITEM_VIEW_TYPE_ROUTINE_HEADER -> RoutineHeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_SESSION -> SavedSessionCardViewHolder.from(parent)
            ITEM_VIEW_TYPE_NO_ROUTINES -> NoRoutinesMessageViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is RoutineListCard.RoutineCard -> ITEM_VIEW_TYPE_ROUTINE
            is RoutineListCard.SavedSessionCard -> ITEM_VIEW_TYPE_SESSION
            RoutineListCard.RoutinesHeader -> ITEM_VIEW_TYPE_ROUTINE_HEADER
            RoutineListCard.NoRoutinesMessage -> ITEM_VIEW_TYPE_NO_ROUTINES
        }
    }

    fun submitListWithSavedSession(
        list: List<RoutineListCard>,
        sessionName: String,
        sessionTime: Long
    ) {
        submitList(
            listOf(
                RoutineListCard.SavedSessionCard(sessionName, Instant.ofEpochMilli(sessionTime)),
            ) + createList(list)
        )
    }

    fun submitListWithHeader(list: List<RoutineListCard>) {
        super.submitList(createList(list))
    }

    private fun createList(list: List<RoutineListCard>): List<RoutineListCard> {
        return listOf(RoutineListCard.RoutinesHeader) +
                (if (list.isEmpty()) listOf(RoutineListCard.NoRoutinesMessage) else list)
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

    object RoutinesHeader : RoutineListCard() {
        override val id = -1L
    }

    object NoRoutinesMessage : RoutineListCard() {
        override val id = -2L
    }

    data class SavedSessionCard(
        val name: String,
        val time: Instant,
        override val id: Long = -1
    ) : RoutineListCard()

    data class RoutineCard(
        override val id: Long,
        val name: String,
        val preview: String
    ) : RoutineListCard()
}