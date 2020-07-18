package com.antwerkz.bottlerocket.configuration

class Free(
        @Added("4.0.0")
        var state: State? = null,
        @Added("4.0.0")
        var tags: String? = null
) : ConfigBlock

enum class State {
    RUNTIME,
    ON,
    OFF
}
