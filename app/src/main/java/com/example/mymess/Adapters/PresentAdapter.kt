package com.example.mymess.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymess.Models.AttendanceItemModel
import com.example.mymess.databinding.PresentItemBinding

class PresentAdapter(
    private val items: MutableList<AttendanceItemModel>,
    private val itemClickListener: OnItemClickListener
): RecyclerView.Adapter<PresentAdapter.PresentViewHolder>() {

    interface OnItemClickListener {
        fun onDeleteClick(date: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PresentItemBinding.inflate(inflater, parent, false)
        return PresentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.sumBy { it.presentDates?.size ?: 0 }
    }

    override fun onBindViewHolder(holder: PresentViewHolder, position: Int) {
        val presentDates = items.flatMap { it.presentDates.orEmpty() }
        val date = presentDates[position]
        holder.bind(date)
    }

    inner class PresentViewHolder(private val binding: PresentItemBinding): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.deleteDate.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val presentDates = items.flatMap { it.presentDates.orEmpty() }
                    val date = presentDates[position]
                    itemClickListener.onDeleteClick(date)
                }
            }
        }

        fun bind(date: String) {
            binding.PresentDate.text = date
        }
    }
}
