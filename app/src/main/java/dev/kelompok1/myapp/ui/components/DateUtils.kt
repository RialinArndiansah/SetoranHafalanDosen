package dev.kelompok1.myapp.ui.components

import android.util.Log
import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.Locale

/**
 * Formats a date string to Western Indonesian Time (WIB) format with full month names.
 * @param dateString The input date string in 'yyyy-MM-dd HH:mm:ss' or 'yyyy-MM-dd' format.
 * @return Formatted date string in WIB timezone with full month names.
 */
fun formatDateToWIB(dateString: String): String {
    if (dateString == "-") return "-"
    Log.d("DateUtils", "Input date string: $dateString")
    
    try {
        // Try to parse full date and time format
        if (dateString.contains(" ") && dateString.length > 10) {
            val parts = dateString.split(" ")[0].split("-")
            val timePart = dateString.split(" ")[1]
            
            if (parts.size == 3) {
                val year = parts[0]
                val month = parts[1].toIntOrNull() ?: 1
                val day = parts[2].toIntOrNull() ?: 1
                
                // Map month number to full Indonesian month name
                val monthName = getFullIndonesianMonth(month)
                
                // Format with time
                return "$day $monthName $year, $timePart WIB"
            }
        } else {
            // Try to parse date-only format
            val parts = dateString.split("-")
            
            if (parts.size == 3) {
                val year = parts[0]
                val month = parts[1].toIntOrNull() ?: 1
                val day = parts[2].toIntOrNull() ?: 1
                
                // Map month number to full Indonesian month name
                val monthName = getFullIndonesianMonth(month)
                
                // Format without time
                return "$day $monthName $year"
            }
        }
        return dateString // Return original if parsing fails
    } catch (e: Exception) {
        Log.d("DateUtils", "Failed to parse date: ${e.message}")
        return dateString // Return original if any exception occurs
    }
}

/**
 * Gets the full Indonesian month name for a given month number (1-12)
 */
fun getFullIndonesianMonth(month: Int): String {
    return when (month) {
        1 -> "Januari"
        2 -> "Februari"
        3 -> "Maret" 
        4 -> "April"
        5 -> "Mei"
        6 -> "Juni"
        7 -> "Juli"
        8 -> "Agustus"
        9 -> "September"
        10 -> "Oktober"
        11 -> "November"
        12 -> "Desember"
        else -> "Januari" // Default
    }
} 