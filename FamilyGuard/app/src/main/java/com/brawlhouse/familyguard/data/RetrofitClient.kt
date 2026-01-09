package com.brawlhouse.familyguard.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // private const val BASE_URL = "http://20.196.128.62:3000/" http://10.0.2.2:3000/   http://10.63.132.76:3000
    private const val BASE_URL = "http://10.63.132.76:3000" // alias ไป host machine 
    // เก็บ Token ที่นี่ตอน Login สำเร็จ
    var authToken: String? = null 

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
            // ถ้ามี Token ให้แนบไปใน Header
            if (authToken != null) {
                requestBuilder.header("Authorization", "Bearer $authToken")
            }
            chain.proceed(requestBuilder.build())
        }
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // อย่าลืมใส่ client
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}