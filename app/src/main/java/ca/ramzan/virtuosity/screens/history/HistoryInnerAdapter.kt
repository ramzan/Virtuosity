package ca.ramzan.virtuosity.screens.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.ramzan.virtuosity.databinding.ListItemHistoryInnerBinding

class HistoryInnerAdapter(private val nameList: List<String>, private val bpmList: List<String>) :
    RecyclerView.Adapter<HistoryInnerAdapter.HistoryInnerViewHolder>() {

    override fun onBindViewHolder(holder: HistoryInnerViewHolder, position: Int) {
        holder.bind(nameList[position], bpmList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryInnerViewHolder {
        return HistoryInnerViewHolder.from(parent)
    }

    class HistoryInnerViewHolder private constructor(private val binding: ListItemHistoryInnerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(name: String, bpm: String) {
            binding.exerciseName.text = name
            binding.maxBpm.text = bpm
        }

        companion object {
            fun from(parent: ViewGroup): HistoryInnerViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding =
                    ListItemHistoryInnerBinding.inflate(layoutInflater, parent, false)

                return HistoryInnerViewHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int {
        return nameList.size
    }
}
