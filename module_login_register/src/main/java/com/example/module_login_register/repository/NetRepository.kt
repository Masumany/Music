package com.example.module_login_register.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.module_login_register.bean.AuthVerifyData
import com.example.module_login_register.bean.PassWordLoginData
import com.example.module_login_register.bean.QrCheckData
import com.example.module_login_register.bean.QrCreateData
import com.example.module_login_register.bean.QrLoginData
import com.example.module_login_register.bean.RefreshData
import com.example.module_login_register.bean.SendData
import com.example.module_login_register.bean.VisitorLoginData
import com.example.module_login_register.netWork.CookieInterceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object NetRepository {

    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.applicationContext
            .getSharedPreferences("cookie", Context.MODE_PRIVATE)
    }

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(CookieInterceptor(sharedPreferences))
        .build()

    private var retrofit = Retrofit.Builder()
        .baseUrl("http://43.139.173.183:3000")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()

    val apiService = retrofit.create(ApiService::class.java)
    interface ApiService {
        @GET("/login/cellphone")
        suspend fun passwordsLogin(
            //将方法参数phone映射为 URL 查询参数
            @Query("phone") phone: String,
            @Query("password") password: String
        ): Response<PassWordLoginData>

        @GET("/captcha/sent")
        suspend fun getAuth(
            @Query("phone") phone: String
        ): Response<SendData>

        @GET("/captcha/verify")
        suspend fun verifyAuth(
            @Query("phone") phone: String,
            @Query("captcha") captcha: String
        ): Response<AuthVerifyData>

        @GET("/login")
        suspend fun mailLogin(
            @Query("email") email: String,
            @Query("password") password: String
        )

        @GET("/login/qr/key")
        suspend fun getQRKey(
            @Query("timestamp") timestamp: String
        ): Response<QrLoginData>

        @GET("/login/qr/create")
        suspend fun createQR(
            @Query("key") key: String,
            @Query("qrimg") qrimg: String
        ): Response<QrCreateData>

        @GET("/login/qr/check")
        suspend fun checkQR(
            @Query("key") key: String
        ): Response<QrCheckData>

        @GET("/login/refresh")
        suspend fun refreshLogin(): Response<RefreshData>

        @GET("/register/anonimous")
        suspend fun visitorLogin(): Response<VisitorLoginData>

        @GET("/register/cellphone")
        suspend fun register(
            @Query("phone") phone: String,
            @Query("password") password: String,
            @Query("captcha") captcha: String,
            @Query("nickname") nickname: String
        )

        @GET("/cellphone/existence/check")
        suspend fun checkRegisterPhone(
            @Query("phone") phone: String
        )

        @GET("/activate/init/profile")
        suspend fun initNickname(
            @Query("nickname") nickname: String
        )

        @GET("/nickname/check")
        suspend fun checkNickname(
            @Query("nickname") nickname: String
        )

    }
}