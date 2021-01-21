package com.flyfun.zap

import android.view.View
import android.widget.Button
import android.widget.LinearLayout

/**
 * @author #Suyghur.
 * Created on 2020/12/7
 */
object DemoButtons {


    private val events: Array<Item> = arrayOf(
            Item(0, "00 崩溃测试"),
            Item(1, "01 打包日志文件"),
            Item(2, "02 删除tmp目录"))

    fun addViews(activity: MainActivity, layout: LinearLayout) {
        for (i in events) {
            val button = Button(activity)
            button.text = i.name
            button.tag = i.id
            button.id = i.id
            button.setOnClickListener(activity)
            layout.addView(button)
        }
    }

    private class Item constructor(val id: Int, val name: String)
}