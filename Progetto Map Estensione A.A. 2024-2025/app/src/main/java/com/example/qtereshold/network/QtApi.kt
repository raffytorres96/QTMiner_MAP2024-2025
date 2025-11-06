package com.example.qtereshold.network

import com.example.qtereshold.data.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface QtApi {
    // Avvia il job di clustering
    @POST("/api/cluster")
    suspend fun start(@Body body: StartRequest): StartResponse

    // Stato del job (progress, stato)
    @GET("/api/cluster/{jobId}/status")
    suspend fun status(@Path("jobId") jobId: String): StatusResponse

    // Risultato finale (clusters + noise)
    @GET("/api/cluster/{jobId}/result")
    suspend fun result(@Path("jobId") jobId: String): ResultResponse
}
