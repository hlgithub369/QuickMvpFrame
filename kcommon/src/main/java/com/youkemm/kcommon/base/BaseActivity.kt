package com.youkemm.kcommon.base

import android.content.Intent
import android.os.Bundle

import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.youkemm.kcommon.R
import com.youkemm.kcommon.facade.CommonLibrary
import com.youkemm.kcommon.http.HttpProvider
import com.blankj.utilcode.util.SPUtils
import com.kennyc.view.MultiStateView
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity


abstract class BaseActivity<out A, out C, P : IBasePresenter, in D> : RxAppCompatActivity(),
        IBaseView<D>, SwipeRefreshLayout.OnRefreshListener {


    protected lateinit var mPresenter: P
    protected val mSPUtils: SPUtils by lazy { SPUtils.getInstance(CommonLibrary.instance.spName) }
    protected val mApiService: A by lazy { HttpProvider.instance.provideApiService<A>() }
    protected val mCacheService: C by lazy { HttpProvider.instance.provideCacheService<C>() }
    protected val mDataMap: HashMap<String, String> by lazy { hashMapOf<String, String>() }
    protected var mSwipeRefresh: SwipeRefreshLayout? = null
    protected var mMultiStateView: MultiStateView? = null
    protected var mErrorView: View? = null
    protected var mTvErrorMsg: TextView? = null
    protected var mBtErrorRetry: Button? = null
    protected var mEmptyView: View? = null
    protected var mTvEmptyMsg: TextView? = null
    protected var mBtEmptyRetry: Button? = null
    private var mBundle: Bundle? = null


    protected abstract val swipeRefreshView: SwipeRefreshLayout?

    protected abstract val multiStateView: MultiStateView?

    protected abstract val layoutResId: Int

    protected abstract val presenter: P

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        mBundle = intent.extras
        mBundle?.let {
            onExtraBundleReceived(it)
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initData()
    }

    override fun onPause() {
        super.onPause()

    }

    override fun onDestroy() {
        super.onDestroy()

    }

    //??????
    override fun onRefresh() {
        mPresenter.initData(mDataMap)
    }

    //??????activity
    override fun startActivity(claz: Class<*>, bundle: Bundle?) {
        val intent = Intent(this, claz)
        bundle?.let { intent.putExtras(it) }
        startActivity(intent)
    }

    //??????activity
    override fun finishActivity() {
        finish()
    }

    //??????Toast
    override fun showTip(tipMsg: String) {
        Toast.makeText(this,tipMsg,Toast.LENGTH_SHORT).show()
//        toast(tipMsg)
    }

    //???????????????
    override fun showLoading() {
        mSwipeRefresh?.isRefreshing = true
    }

    //?????????????????????
    override fun dismissLoading() {
        mSwipeRefresh?.isRefreshing = false
    }

    //??????????????????
    override fun showSuccessView(data: D) {
        mMultiStateView?.viewState = MultiStateView.VIEW_STATE_CONTENT
        showContentView(data)
    }

    //???????????????
    override fun showEmptyView() {
        mMultiStateView?.viewState = MultiStateView.VIEW_STATE_EMPTY
    }

    //??????????????????
    override fun showErrorView(errorMsg: String) {
        mMultiStateView?.viewState = MultiStateView.VIEW_STATE_ERROR
        mTvErrorMsg?.text=errorMsg
    }

    //???????????????????????????????????????
    protected open fun onExtraBundleReceived(bundle: Bundle) {}

    //???????????????
    protected open fun initView() {
        setContentView(layoutResId)
        mBundle = intent.extras
        mBundle?.let {
            onExtraBundleReceived(it)
        }
        mPresenter = presenter
        mSwipeRefresh = swipeRefreshView
        mSwipeRefresh?.setOnRefreshListener(this)
        mMultiStateView = multiStateView
        mMultiStateView?.let {
            mEmptyView = it.getView(MultiStateView.VIEW_STATE_EMPTY)
            mTvEmptyMsg = mEmptyView?.findViewById(R.id.tv_empty_msg)
            mBtEmptyRetry = mEmptyView?.findViewById(R.id.bt_retry)
            mBtEmptyRetry?.setOnClickListener {

                mMultiStateView?.viewState = MultiStateView.VIEW_STATE_LOADING
                mPresenter.initData(mDataMap)
            }

            mErrorView = it.getView(MultiStateView.VIEW_STATE_ERROR)
            mTvErrorMsg = mErrorView?.findViewById(R.id.tv_error_msg)
            mBtErrorRetry = mErrorView?.findViewById(R.id.bt_retry)
            mBtErrorRetry?.setOnClickListener {
                mMultiStateView?.viewState = MultiStateView.VIEW_STATE_LOADING
                mPresenter.initData(mDataMap)
            }
        }
    }

    //??????????????????
    protected abstract fun initData()

    //????????????????????????????????????
    protected abstract fun showContentView(data: D)
}
