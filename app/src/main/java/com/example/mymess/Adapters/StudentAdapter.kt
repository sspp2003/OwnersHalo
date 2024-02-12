package com.example.mymess.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mymess.Models.StudentItemModel
import com.example.mymess.databinding.ListItemBinding

class StudentAdapter(private val items: MutableList<StudentItemModel>): RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        val binding=ListItemBinding.inflate(inflater,parent,false)
        return StudentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val stuitem=items[position]
        holder.bind(stuitem)
    }


    inner class StudentViewHolder(private val binding: ListItemBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(stuitem: StudentItemModel) {
            binding.profileName.text=stuitem.name
            Glide.with(binding.profilePic.context).load(stuitem.profileImage).apply(RequestOptions.circleCropTransform())
                .into(binding.profilePic)
        }

    }
}