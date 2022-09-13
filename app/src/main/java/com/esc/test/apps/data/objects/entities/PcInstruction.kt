package com.esc.test.apps.data.objects.entities

data class PcInstruction(val pos: String, val altPos: String = "") {
    constructor(pos: String): this (pos, "")
}
