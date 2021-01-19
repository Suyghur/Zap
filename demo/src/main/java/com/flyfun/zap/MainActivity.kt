package com.flyfun.zap

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import cn.flyfun.zap.Zap
//import cn.flyfun.zap.Zap
import java.lang.RuntimeException

/**
 * @author #Suyghur,
 * Created on 2021/1/18
 */
class MainActivity : Activity(), View.OnClickListener {

    private lateinit var mTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        Zap.i("flyfun_zap", "info222333444")
    }


    private fun initView() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        DemoButtons.addViews(this, layout)

        mTextView = TextView(this)
        mTextView.text = ""
        layout.addView(mTextView)

        val scrollView = ScrollView(this)
        scrollView.addView(layout)
        setContentView(scrollView)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Zap.flush()
    }

    override fun onDestroy() {
        super.onDestroy()
        Zap.release()
    }

    override fun onClick(v: View?) {
        v?.apply {
            when (tag as Int) {
                9 -> createCrash()
            }
        }
    }

    private fun createCrash() {
        throw RuntimeException("test crash")
    }
}