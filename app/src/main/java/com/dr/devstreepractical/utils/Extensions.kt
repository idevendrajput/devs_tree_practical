package com.dr.devstreepractical.utils

fun Double.formatDecimal(decimals: Int = 2): String {
    return String.format("%.$decimals" + "f", this)
}