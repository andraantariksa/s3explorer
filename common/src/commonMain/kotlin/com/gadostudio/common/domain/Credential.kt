package com.gadostudio.common.domain

data class Credential(
    val accessKey: String,
    val secretKey: String,
    val region: String,
    val endpoint: String,
    val bucket: String
)