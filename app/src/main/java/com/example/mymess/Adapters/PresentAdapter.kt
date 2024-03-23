package com.example.mymess.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymess.Models.AttendanceItemModel
import com.example.mymess.databinding.PresentItemBinding

class PresentAdapter(
    private val items: MutableList<AttendanceItemModel>
) : RecyclerView.Adapter<PresentAdapter.PresentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PresentItemBinding.inflate(inflater, parent, false)
        return PresentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.sumBy { it.presentDates?.size ?: 0 }
    }

    override fun onBindViewHolder(holder: PresentViewHolder, position: Int) {
        val item = items.flatMap { it.presentDates.orEmpty() }
        holder.bind(item.getOrNull(position))
    }

    inner class PresentViewHolder(private val binding: PresentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(date: String?) {
            // Check if date is not null before binding
            date?.let { binding.PresentDate.text = it }
        }
    }
}
