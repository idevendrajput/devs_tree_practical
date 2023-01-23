package com.dr.devstreepractical.base

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.dr.devstreepractical.ui.dialogs.Loading

abstract class BaseActivity<T: ViewBinding>() : AppCompatActivity() {

    private lateinit var loading: Loading
    protected lateinit var binding: T

    protected abstract fun getViewBinding(): T

    abstract fun init()

    val TAG = javaClass.name

    lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()
        mContext = this
        setContentView(binding.root)
        loading = Loading(this).build()
        init()
    }

    fun showLoading() {
        loading.showLoading()
    }

    fun showLoading(msg: String) {
        loading.showLoading(msg)
    }

    fun dismissLoading() {
        loading.dismissLoading()
    }

    fun isLoadingCancelable(isCancelable: Boolean = false) {
        loading.setCancelable(isCancelable)
    }

}