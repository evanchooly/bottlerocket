package com.antwerkz.bottlerocket.configuration

@Retention
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class Mode(val value: ConfigMode)