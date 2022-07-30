package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.BottleRocket

data class PortAllocator(private var port: Int = BottleRocket.DEFAULT_PORT) {
    fun next() = port++
}
