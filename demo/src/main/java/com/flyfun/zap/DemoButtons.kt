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
            Item(0, "00 接口环境切换"),
            Item(1, "01 登录"),
            Item(2, "02 切换账号"),
            Item(3, "03 角色创建上报"),
            Item(4, "04 角色登录上报"),
            Item(5, "05 角色升级上报"),
            Item(6, "06 定额充值"),
            Item(7, "07 绑定平台账号"),
            Item(8, "08 打开客服中心"),
            Item(9, "09 崩溃测试"))


    private var bindButton: Button? = null
    private var gmButton: Button? = null

    fun hideBindButton() {
        bindButton?.apply {
            if (visibility == View.VISIBLE) {
                visibility = View.GONE
            }
        }
    }

    fun showBindButton() {
        bindButton?.apply {
            if (visibility == View.GONE) {
                visibility = View.VISIBLE
            }
        }
    }

    fun hideGMButton() {
        gmButton?.apply {
            if (visibility == View.VISIBLE) {
                visibility = View.GONE
            }
        }
    }

    fun showGMButton() {
        gmButton?.apply {
            if (visibility == View.GONE) {
                visibility = View.VISIBLE
            }
        }
    }

    fun addViews(activity: MainActivity, layout: LinearLayout) {
        for (i in events) {
            when (i.id) {
                7 -> {
                    bindButton = Button(activity)
                    bindButton?.apply {
                        text = i.name
                        tag = i.id
                        id = i.id
                        setOnClickListener(activity)
                        visibility = View.GONE
                        layout.addView(this)
                    }
                }
                8 -> {
                    gmButton = Button(activity)
                    gmButton?.apply {
                        text = i.name
                        tag = i.id
                        id = i.id
                        setOnClickListener(activity)
                        visibility = View.GONE
                        layout.addView(this)
                    }
                }
                else -> {
                    val button = Button(activity)
                    button.text = i.name
                    button.tag = i.id
                    button.id = i.id
                    button.setOnClickListener(activity)
                    layout.addView(button)
                }
            }
        }

    }

    private class Item constructor(val id: Int, val name: String)
}