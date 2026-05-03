package com.garent.s35123656.medtrack.data.repository

import com.garent.s35123656.medtrack.data.remote.DrugInfo
import com.garent.s35123656.medtrack.data.remote.OpenFdaService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DrugRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.fda.gov/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(OpenFdaService::class.java)

    suspend fun getDrugDetails(brandName: String): Result<DrugInfo?> {
        return try {
            val response = service.searchDrug("openfda.brand_name:\"$brandName\"")
            Result.success(response.results?.firstOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
