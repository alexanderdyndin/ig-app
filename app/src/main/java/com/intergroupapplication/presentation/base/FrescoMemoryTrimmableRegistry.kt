package com.intergroupapplication.presentation.base

import com.facebook.common.memory.MemoryTrimType
import com.facebook.common.memory.MemoryTrimmable
import com.facebook.common.memory.MemoryTrimmableRegistry
import java.util.*


class FrescoMemoryTrimmableRegistry : MemoryTrimmableRegistry {
    private val trimmables: MutableList<MemoryTrimmable> = LinkedList()

    override fun registerMemoryTrimmable(trimmable: MemoryTrimmable) {
        trimmables.add(trimmable)
    }

    override fun unregisterMemoryTrimmable(trimmable: MemoryTrimmable) {
        trimmables.remove(trimmable)
    }

    @Synchronized
    fun trim(trimType: MemoryTrimType?) {
        for (trimmable in trimmables) {
            trimmable.trim(trimType)
        }
    }
}