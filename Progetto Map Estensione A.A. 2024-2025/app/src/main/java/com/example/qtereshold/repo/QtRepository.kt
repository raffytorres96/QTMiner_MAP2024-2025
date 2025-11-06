package com.example.qtereshold.repo

import com.example.qtereshold.data.*
import com.example.qtereshold.network.QtApi
import kotlinx.coroutines.delay

class QtRepository(private val api: QtApi) {

    suspend fun startJob(params: QtParams, data: List<Point>): StartResponse {
        return api.start(StartRequest(params, data))
    }

    suspend fun pollUntilDone(jobId: String, onProgress: (Int) -> Unit): ResultResponse {
        while (true) {
            val s = api.status(jobId)
            onProgress(s.progress.coerceIn(0, 100))
            when (s.state.uppercase()) {
                "DONE" -> break
                "FAILED" -> throw RuntimeException("Job fallito")
                else -> delay(600) // mezzo secondo circa
            }
        }
        return api.result(jobId)
    }
}
