package com.flyfun.zap

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import cn.flyfun.zap.ZapFileUtils
import cn.flyfun.zap.Zap
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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
            val logFiles = ZapFileUtils.getAllLogFiles(this)
            for (log in logFiles) {
                ZapFileUtils.copyFile(File("$this/$log"), File("$this/tmp/$log"))
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
//            val path = getExternalFilesDir("zap")?.absolutePath
            when (tag as Int) {
                0 -> createCrash()
                1 -> {
                    ZapFileUtils.packLogFiles(this@MainActivity)
                }
                2 -> ZapFileUtils.deleteFile(getExternalFilesDir("zap")?.absolutePath + "/tmp")
            }
        }
    }

    private fun createCrash() {
        throw RuntimeException("test crash")
    }

}