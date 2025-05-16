package com.example.setoranhafalanapp.data.network

import com.example.setoranhafalan.data.model.AuthResponse
import com.example.setoranhafalan.data.model.DosenResponse
import com.example.setoranhafalanapp.data.model.SetoranMahasiswaResponse
import com.example.setoranhafalanapp.data.model.SetoranResponse
import com.example.setoranhafalanapp.data.model.SetoranRequest
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("/realms/dev/protocol/openid-connect/token")
    suspend fun login(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("scope") scope: String
    ): Response<AuthResponse>

    @FormUrlEncoded
    @POST("/realms/dev/protocol/openid-connect/token")
    suspend fun refreshToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String
    ): Response<AuthResponse>

    @GET("dosen/pa-saya")
    suspend fun getDosenInfo(
        @Header("Authorization") token: String
    ): Response<DosenResponse>

    @GET("mahasiswa/setoran/{nim}")
    suspend fun getSetoranMahasiswa(
        @Header("Authorization") token: String,
        @Path("nim") nim: String
    ): Response<SetoranMahasiswaResponse>

    @Headers("Content-Type: application/json")
    @POST("mahasiswa/setoran/{nim}")
    suspend fun postSetoranMahasiswa(
        @Header("Authorization") token: String,
        @Path("nim") nim: String,
        @Body request: SetoranRequest
    ): Response<SetoranResponse>

    @Headers("Content-Type: application/json")
    @HTTP(method = "DELETE", path = "mahasiswa/setoran/{nim}", hasBody = true)
    suspend fun deleteSetoranMahasiswa(
        @Header("Authorization") token: String,
        @Path("nim") nim: String,
        @Query("id") id: String,
        @Body request: SetoranRequest
    ): Response<SetoranResponse>
}