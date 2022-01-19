package com.antwerkz.bottlerocket.configuration

@Retention
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class Removed(val value: String)
