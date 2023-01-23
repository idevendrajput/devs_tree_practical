package com.dr.devstreepractical.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.dr.devstreepractical.R
import com.dr.devstreepractical.databinding.RowItemLocationBinding
import com.dr.devstreepractical.room.LocationsEntity
import com.dr.devstreepractical.utils.AppFunctions
import com.dr.devstreepractical.utils.formatDecimal

class AdapterLocations(
    private val list: ArrayList<LocationsEntity>,
    val onEdit: (id: Long) -> Unit,
    val onDelete: (id: Long, onDeleteDone: () -> Unit) -> Unit
) : RecyclerView.Adapter<AdapterLocations.Holder>() {

    class Holder(itemView: View, val binding: RowItemLocationBinding) :
        RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = RowItemLocationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(
            binding.root, binding
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        val binding = holder.binding

        binding.cityName.text = list[position].cityName
        binding.address.text = list[position].address

        binding.distance.text = "${list[position].distance.formatDecimal()} KM"

        binding.edit.setOnClickListener {
            onEdit(list[position].id ?: -1)
        }

        binding.delete.setOnClickListener {
            onDelete(list[position].id ?: -1) {
                list.removeAt(position)
                notifyItemRemoved(position)
            }
        }


    }

    override fun getItemCount() = list.size

}