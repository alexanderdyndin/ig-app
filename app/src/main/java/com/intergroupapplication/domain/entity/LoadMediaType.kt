package com.intergroupapplication.domain.entity

enum class LoadMediaType(var progress:Float = -1f)
{
    START(0.0f),
    PROGRESS(0.0f),
    ERROR,
    UPLOAD(100.0f)
}