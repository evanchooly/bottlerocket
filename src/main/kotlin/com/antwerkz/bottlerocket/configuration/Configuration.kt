package com.antwerkz.bottlerocket.configuration

public interface Configuration: ConfigBlock {
    fun isAuthEnabled(): Boolean;

    override fun nodeName(): String {
        return "configuration"
    }

    override fun toMap(mode: ConfigMode, includeAll: Boolean): Map<String, Any> {
          return super.toMap(mode, includeAll).get("configuration") as Map<String, Any>
    }
}