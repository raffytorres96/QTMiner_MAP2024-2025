package com.example.qtereshold.data

// Parametri Quality Threshold
data class QtParams(
    val radius: Double,
    val minClusterSize: Int,
    val distance: String // "euclidean" | "manhattan" (stringa libera per ora)
)

// Punto generico (2D per semplicità)
data class Point(
    val id: String,
    val x: Double,
    val y: Double
)

// Centro cluster (opzionale nei risultati)
data class Center(val x: Double, val y: Double)

// Cluster risultante
data class Cluster(
    val id: Int,
    val members: List<String>,
    val center: Center? = null,
    val diameter: Double? = null
)

// --- Request/Response per le API ---
data class StartRequest(val params: QtParams, val data: List<Point>)
data class StartResponse(val jobId: String)
data class StatusResponse(val state: String, val progress: Int) // state: RUNNING|DONE|FAILED
data class ResultResponse(val jobId: String, val clusters: List<Cluster>, val noise: List<String>)