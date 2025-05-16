package com.example.setoranhafalanapp.data.model

import com.google.gson.annotations.SerializedName


data class SetoranMahasiswaResponse(
    val response: Boolean,
    val message: String,
    val data: SetoranMahasiswaData
)

data class SetoranMahasiswaData(
    val info: MahasiswaInfo,
    val setoran: SetoranInfo
)

data class MahasiswaInfo(
    val nama: String,
    val nim: String,
    val email: String,
    val angkatan: String,
    val semester: Int,
    val dosen_pa: DosenPengesah
)

data class DosenPengesah(
    val nip: String,
    val nama: String,
    val email: String
)

data class SetoranInfo(
    val log: List<SetoranLog>,
    val info_dasar: InfoDasar,
    val ringkasan: List<Ringkasan>,
    val detail: List<SetoranDetail>
)

data class SetoranLog(
    val id: Int,
    val keterangan: String,
    val aksi: String,
    val ip: String,
    val user_agent: String,
    val timestamp: String,
    val nim: String,
    val dosen_yang_mengesahkan: DosenPengesah
)

data class InfoDasar(
    val total_wajib_setor: Int,
    val total_sudah_setor: Int,
    val total_belum_setor: Int,
    val persentase_progres_setor: Double,
    val tgl_terakhir_setor: String?,
    val terakhir_setor: String
)

data class Ringkasan(
    val label: String,
    val total_wajib_setor: Int,
    val total_sudah_setor: Int,
    val total_belum_setor: Int,
    val persentase_progres_setor: Double
)

data class SetoranDetail(
    val id: String,
    val nama: String,
    val label: String,
    val sudah_setor: Boolean,
    val info_setoran: InfoSetoran?
)

data class InfoSetoran(
    val id: String,
    val tgl_setoran: String,
    val tgl_validasi: String,
    val dosen_yang_mengesahkan: DosenPengesah
)



data class SetoranResponse(
    val response: Boolean,
    val keterangan: String,
    val label: String,
    val message: String,
    val data: SetoranDetail?,
    val id_komponen_setoran: String,
)
data class SetoranRequest(
    @SerializedName("data_setoran")
    val dataSetoran: List<SetoranItem>,
    @SerializedName("tgl_setoran")
    val tglSetoran: String? = null
)

data class SetoranItem(
    @SerializedName("id")
    val id: String? = null, // Untuk DELETE
    @SerializedName("id_komponen_setoran")
    val idKomponenSetoran: String,
    @SerializedName("nama_komponen_setoran")
    val namaKomponenSetoran: String
)