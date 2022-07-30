package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.configuration.Configuration

interface Configurable {
    fun configure(update: Configuration)

    fun configure(update: Configuration.() -> Unit) {
        val configuration = Configuration()
        configuration.update()

        configure(configuration)
    }
}