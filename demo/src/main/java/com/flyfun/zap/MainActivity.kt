package com.flyfun.zap

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import cn.flyfun.zap.toolkit.FileUtils
import cn.flyfun.zap.Zap
import java.io.File
import java.util.*

/**
 * @author #Suyghur,
 * Created on 2021/1/18
 */
class MainActivity : Activity(), View.OnClickListener {

    private lateinit var mTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        val path = getExternalFilesDir("zap")?.absolutePath
        path?.apply {
            val logFiles = FileUtils.getAllLogFiles(this)
            for (log in logFiles) {
                FileUtils.copyFile(File("$this/$log"), File("$this/tmp/$log"))
            }
        }
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
                0 -> createRuntimeExceptionCrash()
                1 -> createException()
                2 -> FileUtils.packLogFiles(this@MainActivity)
                3 -> FileUtils.deleteFile(getExternalFilesDir("zap")?.absolutePath + "/tmp")
                4 -> Zap.d("DEBUG级别日志测试")
                5 -> Zap.i("INFO级别日志测试")
                6 -> Zap.e("ERROR级别日志测试")
            }
        }
    }

    private fun createRuntimeExceptionCrash() {
        throw RuntimeException("test runtime exception crash")
    }

    private fun createException() {
        throw Exception("test exception crash")
    }

}