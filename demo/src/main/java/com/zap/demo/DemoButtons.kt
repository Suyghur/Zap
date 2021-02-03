package com.zap.demo

import android.widget.Button
import android.widget.LinearLayout

/**
 * @author #Suyghur.
 * Created on 2020/12/7
 */
object DemoButtons {


    private val events: Array<Item> = arrayOf(
            Item(0, "00 崩溃Runtime测试"),
            Item(1, "01 崩溃exception"),
            Item(2, "02 删除tmp目录"),
            Item(3, "03 删除tmp目录"),
            Item(4, "04 DEBUG级别日志测试"),
            Item(5, "05 INFO级别日志测试"),
            Item(6, "06 ERROR级别日志测试"))


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