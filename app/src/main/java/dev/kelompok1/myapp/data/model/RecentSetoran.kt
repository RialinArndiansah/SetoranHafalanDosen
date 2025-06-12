package dev.kelompok1.myapp.data.model

data class RecentSetoran(
    val id: String = "", // ID for tracking when a setoran is deleted
    val nim: String,
    val nama: String,
    val angkatan: String,
    val komponenSetoran: String,
    val tanggalSetoran: String,
    val formattedDate: String
) 