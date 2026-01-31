package com.dnavarro.neospectro.utils

fun <T> MutableList<T>.onBack() {
    if (size > 1) removeLastOrNull()
}