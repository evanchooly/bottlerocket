package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.BottleRocket

object PortAllocator {
    private var port: Int = BottleRocket.DEFAULT_PORT

    fun basePort(value: Int) {
        port = value
    }

    fun next() = port++
}
