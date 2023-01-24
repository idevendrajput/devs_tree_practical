package com.dr.devstreepractical.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.room.Index
import com.dr.devstreepractical.R
import com.dr.devstreepractical.base.BaseFragment
import com.dr.devstreepractical.databinding.FragmentLocationsBinding
import com.dr.devstreepractical.ui.adapters.AdapterLocations
import com.dr.devstreepractical.utils.AppConst
import com.dr.devstreepractical.utils.AppConst.IS_ROUTE_MODE
import com.dr.devstreepractical.utils.AppFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Locations : BaseFragment<FragmentLocationsBinding>() {

    var order = Index.Order.ASC

    private val job = CoroutineScope(IO)

    override fun init() {

        loadData()

    }

    private fun loadData() {

        job.launch {

            val locations = AppFunctions.getLocations(room, order)

            Log.d(TAG, "loadData: ${locations.size}")
            withContext(Main) {

                binding.noResult.isVisible = locations.isEmpty()

                binding.rv.adapter = AdapterLocations(
                    ArrayList(locations),
                    onEdit = {
                        findNavController().navigate(
                            R.id.action_locations_to_map, Bundle().apply {
                                putLong(AppConst.LOCATION_ID, it)
                            }
                        )
                    },
                    onDelete = { id, onDeleteDone ->
                        confirmDeleteDialog {
                            room?.locationDao()?.deleteById(id).also {
                                onDeleteDone()
                            }
                        }
                    }
                )

            }
        }

    }

    private fun handleSortingChange() {
        sortingDialog {
            loadData()
        }
    }

    private fun sortingDialog(onSelection: () -> Unit) {
        AlertDialog.Builder(mContext)
            .setTitle("Sort By")
            .setItems(arrayOf("Ascending", "Descending")) { _, i ->
                when (i) {
                    0 -> order = Index.Order.ASC
                    1 -> order = Index.Order.DESC
                }
                onSelection()
            }.create().show()
    }

    private fun confirmDeleteDialog(onDelete: () -> Unit) {
        AlertDialog.Builder(mContext)
            .setTitle("Delete")
            .setMessage("Are you sure you want to delete this location?")
            .setPositiveButton("Yes") { _, _ ->
                onDelete()
            }.setNegativeButton("", null).create().show()
    }

    override fun actions() {

        binding.addPOI.setOnClickListener {
            findNavController().navigate(R.id.action_locations_to_map)
        }

        binding.tb.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_locations_to_map, Bundle().apply { putBoolean(IS_ROUTE_MODE, true) })
        }

        binding.tb.menu[0].setOnMenuItemClickListener {
            handleSortingChange()
            false
        }

    }

    override fun getViewBinding() = FragmentLocationsBinding.inflate(layoutInflater)

}