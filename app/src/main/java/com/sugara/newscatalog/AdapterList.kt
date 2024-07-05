package com.sugara.newscatalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sugara.newscatalog.ItemList


class AdapterList(private val itemLists : List<ItemList>):RecyclerView.Adapter<AdapterList.ViewHolder>() {
    private lateinit var listener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(item: ItemList)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class ViewHolder (@NonNull itemView: View) : RecyclerView. ViewHolder (itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_image)
        val title: TextView = itemView.findViewById(R.id.title)
        val desc: TextView = itemView.findViewById (R.id.desc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterList.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdapterList.ViewHolder, position: Int) {
        val item = itemLists[position]
        holder.title.text = item.title
        holder.desc.text = item.desc
        Glide.with(holder.imageView.context)
            .load(item.imageToUse)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            listener.onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return itemLists.size
    }
}