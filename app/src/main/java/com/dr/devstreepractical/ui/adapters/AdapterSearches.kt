package com.dr.devstreepractical.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.dr.devstreepractical.room.LocationsEntity

class AdapterSearches(private val list: List<LocationsEntity>, val callBack: (place: LocationsEntity) -> Unit): Adapter<AdapterSearches.Holder>() {

    class Holder(itemView: View) : ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
         return Holder(
             LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
         )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        val city = holder.itemView.findViewById<TextView>(android.R.id.text1)
        val address = holder.itemView.findViewById<TextView>(android.R.id.text2)

        city.text = list[position].cityName
        address.text = list[position].address

        holder.itemView.setOnClickListener {
            callBack(list[position])
        }

    }

    override fun getItemCount() = list.size

}