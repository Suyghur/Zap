package cn.flyfun.zap

/**
 * @author #Suyghur,
 * Created on 2021/1/15
 */
class ZapData {
    var level = 0
    var tag: String = ""
    var msg: String = ""
    private var next: ZapData? = null


    fun recycle() {
        level = 0
        tag = ""
        msg = ""
        synchronized(sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                next = sPool
                sPool = this
                sPoolSize++
            }
        }
    }

    companion object {
        private val sPoolSync = Any()
        private var sPool: ZapData? = null
        private var sPoolSize = 0
        private const val MAX_POOL_SIZE = 50

        fun obtain(): ZapData {
            synchronized(sPoolSync) {
                if (sPool != null) {
                    val m = sPool
                    sPool = m!!.next
                    m.next = null
                    sPoolSize--
                    return m
                }
            }
            return ZapData()
        }

        fun obtain(level: Int, tag: String, msg: String): ZapData {
            val ob = obtain()
            ob.level = level
            ob.tag = tag
            ob.msg = msg
            return ob
        }
    }
}