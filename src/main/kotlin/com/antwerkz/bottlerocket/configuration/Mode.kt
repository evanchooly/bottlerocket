package com.antwerkz.bottlerocket.configuration

@Retention
@Repeatable
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class Mode(val value: ConfigMode)