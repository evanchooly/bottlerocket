package com.antwerkz.bottlerocket.clusters

data class PortAllocator(private var port: Int) {
    fun next() = port++
}
