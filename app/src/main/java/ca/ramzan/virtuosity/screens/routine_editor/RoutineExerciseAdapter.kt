package ca.ramzan.virtuosity.screens.routine_editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.underline
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.ramzan.virtuosity.common.millisToTimerString
import ca.ramzan.virtuosity.databinding.ListItemAddExerciseButtonBinding
import ca.ramzan.virtuosity.databinding.ListItemRoutineExerciseBinding
import ca.ramzan.virtuosity.routine.RoutineExercise

private const val ITEM_VIEW_TYPE_EXERCISE = 0
const val ITEM_VIEW_TYPE_ADD_BUTTON = 1

class RoutineExerciseAdapter(
    private val onDurationClick: (exerciseIndex: Int, duration: Long) -> Unit,
    private val onDeleteClick: (exerciseIndex: Int) -> Unit,
    private val onAddExerciseClick: () -> Unit
) : ListAdapter<RoutineEditorListItem, RecyclerView.ViewHolder>(
    RoutineDiffCallback()
) {

    fun submitWithFooter(list: MutableList<RoutineExercise>) {
        super.submitList(
            list.map { RoutineEditorListItem.ExerciseRow(it) }
                    + listOf(RoutineEditorListItem.AddExerciseButton)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ExerciseViewHolder -> holder.bind(
                (item as RoutineEditorListItem.ExerciseRow).exercise,
                onDurationClick,
                onDeleteClick
            )
            is AddExerciseButtonViewHolder -> holder.itemView.setOnClickListener {
                onAddExerciseClick()
            }

            else -> throw Exception("Illegal holder type: $holder")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_EXERCISE -> ExerciseViewHolder.from(parent)
            ITEM_VIEW_TYPE_ADD_BUTTON -> AddExerciseButtonViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == currentList.size - 1) ITEM_VIEW_TYPE_ADD_BUTTON else ITEM_VIEW_TYPE_EXERCISE
    }

    class ExerciseViewHolder private constructor(private val binding: ListItemRoutineExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: RoutineExercise,
            onDurationClick: (exerciseIndex: Int, duration: Long) -> Unit,
            onDeleteClick: (exerciseIndex: Int) -> Unit
        ) {
            binding.apply {
                exerciseName.text = item.name
                duration.text = buildSpannedString {
                    underline { append(millisToTimerString(item.duration)) }
                }
                duration.setOnClickListener {
                    onDurationClick(bindingAdapterPosition, item.duration)
                }
                deleteBtn.setOnClickListener {
                    onDeleteClick(bindingAdapterPosition)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ExerciseViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding =
                    ListItemRoutineExerciseBinding.inflate(layoutInflater, parent, false)

                return ExerciseViewHolder(binding)
            }
        }
    }

    class AddExerciseButtonViewHolder private constructor(binding: ListItemAddExerciseButtonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): AddExerciseButtonViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding =
                    ListItemAddExerciseButtonBinding.inflate(layoutInflater, parent, false)

                return AddExerciseButtonViewHolder(binding)
            }
        }
    }
}

class RoutineDiffCallback : DiffUtil.ItemCallback<RoutineEditorListItem>() {
    override fun areItemsTheSame(
        oldItem: RoutineEditorListItem,
        newItem: RoutineEditorListItem
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: RoutineEditorListItem,
        newItem: RoutineEditorListItem
    ): Boolean {
        return oldItem == newItem
    }
}

sealed class RoutineEditorListItem {

    abstract val id: Long

    object AddExerciseButton : RoutineEditorListItem() {
        override val id = -1L
    }

    class ExerciseRow(val exercise: RoutineExercise) :
        RoutineEditorListItem() {
        // This causes an odd swiping animation if you have the same exercise listed multiple
        // times in a row and delete one of them, since they would have the same id.
        // However, functionality is not affected.
        override val id = exercise.exerciseId
    }
}