package com.example.basicfetch.network

import com.example.basicfetch.models.Item
import retrofit2.http.GET
interface ApiService {
    @GET("hiring.json")
    suspend fun fetchItems(): List<Item>
}
