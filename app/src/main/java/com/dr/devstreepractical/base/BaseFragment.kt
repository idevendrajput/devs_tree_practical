package com.dr.devstreepractical.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.dr.devstreepractical.room.MyDatabase
import com.dr.devstreepractical.ui.dialogs.Loading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

abstract class BaseFragment<T: ViewBinding> : Fragment(), CoroutineScope {

    private lateinit var loading: Loading
    protected lateinit var binding: T
    open lateinit var mContext: Context
    var room: MyDatabase? = null

    abstract fun init()

    open fun actions() { }

    val TAG = javaClass.name

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    protected abstract fun getViewBinding(): T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = getViewBinding()
        loading = Loading(mContext).build()
        room = MyDatabase.getDatabase(mContext)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        init()
        actions()
    }

    fun showLoading() {
        if (this::loading.isInitialized)
            loading.showLoading()
    }

    fun showLoading(msg: String) {
        if (this::loading.isInitialized)
            loading.showLoading(msg)
    }

    fun dismissLoading() {
        if (this::loading.isInitialized)
            loading.dismissLoading()
    }

    fun isLoadingCancelable(isCancelable: Boolean = false) {
        if (this::loading.isInitialized)
            loading.setCancelable(isCancelable)
    }

}