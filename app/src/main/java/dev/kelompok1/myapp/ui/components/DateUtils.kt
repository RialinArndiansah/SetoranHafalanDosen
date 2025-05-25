package dev.kelompok1.myapp.ui.components

import androidx.compose.runtime.Composable
import android.util.Log
import java.text.SimpleDateFormat
import java.util.TimeZone

/**
 * Formats a date string to Western Indonesian Time (WIB) format.
 * @param dateString The input date string in 'yyyy-MM-dd HH:mm:ss' or 'yyyy-MM-dd' format.
 * @return Formatted date string in WIB timezone.
 */
@Composable
fun formatDateToWIB(dateString: String): String {
    if (dateString == "-") return "-"
    Log.d("DateUtils", "Input date string: $dateString")
    return try {
        // Try full date and time format
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm WIB")
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        outputFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        val date = inputFormat.parse(dateString)
        Log.d("DateUtils", "Successfully parsed as full date-time")
        outputFormat.format(date)
    } catch (e: Exception) {
        Log.d("DateUtils", "Failed to parse as full date-time: ${e.message}")
        try {
            // Fallback to date only format
            val inputFormat = SimpleDateFormat("yyyy-MM-dd")
            val outputFormat = SimpleDateFormat("dd MMM yyyy")
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            outputFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
            val date = inputFormat.parse(dateString)
            Log.d("DateUtils", "Successfully parsed as date-only")
            outputFormat.format(date)
        } catch (e2: Exception) {
            Log.d("DateUtils", "Failed to parse as date-only: ${e2.message}")
            "Format tanggal tidak valid"
        }
    }
} 