package com.example.pathmasterproject.utils

fun formatTime(seconds: Long): String {
    val days = seconds / (24 * 3600)
    val hours = (seconds % (24 * 3600)) / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02dd : %02dh : %02dm : %02ds", days, hours, minutes, secs)
}