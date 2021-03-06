package com.youkemm.kcommon.base

import android.view.ViewGroup
import com.youkemm.kcommon.R
import com.youkemm.kcommon.entity.net.Optional
import com.youkemm.kcommon.facade.CommonLibrary
import com.youkemm.kcommon.widget.CustomLoadMoreView
import com.chad.library.adapter.base.BaseQuickAdapter

import com.kennyc.view.MultiStateView

abstract class BaseRefreshAndLoadMoreActivity<out A, out C, P : IBaseRefreshAndLoadMorePresenter, D> :
        BaseActivity<A, C, P, D>(), BaseQuickAdapter.RequestLoadMoreListener,
        IBaseRefreshAndLoadMoreView<D> {
    protected var mAdapter: BaseQuickAdapter<*, *>? = null
    private var mIsLoadComplete = false
    private var mCurPage = CommonLibrary.instance.startPage
    private val PAGE_SIZE by lazy { CommonLibrary.instance.pageSize }
    private var mRecyclerView: androidx.recyclerview.widget.RecyclerView? = null
    private var mLayoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null

    protected val emptyLayoutId: Int
        get() = R.layout.layout_empty

    protected abstract val adapter: BaseQuickAdapter<*, *>?

    protected abstract val recyclerView: androidx.recyclerview.widget.RecyclerView?

    protected abstract val layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager?

    override fun initView() {

        super.initView()
        mRecyclerView = recyclerView
        mLayoutManager = layoutManager
        mRecyclerView?.layoutManager = mLayoutManager
        mAdapter = adapter
        val noDataView = layoutInflater.inflate(
                emptyLayoutId, mRecyclerView?.parent as ViewGroup, false)
        mAdapter?.emptyView = noDataView
        mAdapter?.setLoadMoreView(CustomLoadMoreView())
        mAdapter?.setOnLoadMoreListener(this, mRecyclerView)
        mRecyclerView?.adapter = mAdapter
        mAdapter?.disableLoadMoreIfNotFullPage()
    }

    override fun showSuccessView(data: D) {
        mMultiStateView?.viewState = MultiStateView.VIEW_STATE_CONTENT
        if (data is Optional<*>) {
            if (data.data is List<*>) {
                mAdapter?.setNewData(data.data as List<Nothing>)
            } else {
                mAdapter?.setNewData((data.data as ILoadMoreData).list as List<Nothing>)
            }
        }else{
            if (data is List<*>) {
                mAdapter?.setNewData(data as List<Nothing>)
            } else {
                mAdapter?.setNewData((data as ILoadMoreData).list as List<Nothing>)
            }
        }
        showContentView(data)
    }

    override fun beforeInitData() {
        mAdapter?.setEnableLoadMore(false)
        mIsLoadComplete = false
        mCurPage = 1
    }

    override fun afterLoadMore(data: D) {
        if (data is Optional<*>) {
            if (data.data is List<*>) {
                mIsLoadComplete = (data.data as List<*>).size < PAGE_SIZE
                mAdapter?.addData(data.data as List<Nothing>)
            } else {
                mIsLoadComplete = (data.data as ILoadMoreData).list.size < PAGE_SIZE
                mAdapter?.addData((data.data as ILoadMoreData).list as List<Nothing>)
            }
        }else{
            if (data is List<*>) {
                mIsLoadComplete = (data as List<*>).size < PAGE_SIZE
                mAdapter?.addData(data as List<Nothing>)
            } else {
                mIsLoadComplete = (data as ILoadMoreData).list.size < PAGE_SIZE
                mAdapter?.addData((data as ILoadMoreData).list as List<Nothing>)
            }
        }
        mCurPage++
        mAdapter?.loadMoreComplete()
        mSwipeRefresh?.isEnabled = true
    }

    override fun afterLoadMoreError(e: Throwable) {
        mAdapter?.loadMoreFail()
        mSwipeRefresh?.isEnabled = true
    }

    override fun onLoadMoreRequested() {
        mSwipeRefresh?.isEnabled = false
        mAdapter?.let {
            if (it.data.size < PAGE_SIZE) {
                it.loadMoreEnd(false)
                mSwipeRefresh?.isEnabled = true
            } else {
                if (mIsLoadComplete) {
                    it.loadMoreEnd()
                    mSwipeRefresh?.isEnabled = true
                } else {
                    mPresenter.initData(mDataMap, mCurPage + 1)
                }
            }
        }
    }
}
