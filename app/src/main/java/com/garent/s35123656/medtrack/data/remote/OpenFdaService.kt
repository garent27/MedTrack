package com.garent.s35123656.medtrack.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFdaService {
    @GET("drug/label.json")
    suspend fun searchDrug(
        @Query("search") query: String,
        @Query("limit") limit: Int = 1
    ): OpenFdaResponse
}

data class OpenFdaResponse(
    val results: List<DrugInfo>?
)

data class DrugInfo(
    val purpose: List<String>?,
    val warnings: List<String>?,
    val dosage_and_administration: List<String>?,
    val indications_and_usage: List<String>?,
    val active_ingredient: List<String>?
)
