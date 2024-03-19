package com.example.mymess.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mymess.Models.BalanceItemModel
import com.example.mymess.StudentBalanceActivity
import com.example.mymess.databinding.BalanceItemBinding
import com.example.mymess.databinding.ListItemBinding

class BalanceAdapter(private val items: MutableList<BalanceItemModel>): RecyclerView.Adapter<BalanceAdapter.BalanceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BalanceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BalanceItemBinding.inflate(inflater, parent, false)
        return BalanceViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: BalanceViewHolder, position: Int) {
        val balitem=items[position]
        holder.bind(balitem)
    }

    inner class BalanceViewHolder(private val binding: BalanceItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(balitem: BalanceItemModel) {
            binding.messStartDate.text=balitem.startDate
            binding.messEndDate.text=balitem.endDate
            binding.balanceAmount.text=balitem.balanceamount
        }

    }
}