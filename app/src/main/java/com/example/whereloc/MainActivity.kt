package com.example.whereloc

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.telecom.Call
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.security.auth.callback.Callback


@DelicateCoroutinesApi
class MainActivity : AppCompatActivity() {
    val EXTRA_MESSAGE: String = "com.example.whereloc.MESSAGE"

    // network policy: https://developer.android.com/training/articles/security-config#CleartextTrafficPermitted
    // デフォルトで暗号化されていない通信はできない -> 暗号化された通信をする or 設定の変更
    //   1. https で暗号化された通信をする
    //   2. AndroidManifest.xml に usersCleartextTraffic=true を設定する
    //      ※暗号化されていない通信をすべて許可するので注意
    //   3. network_security_config.xml を設定して細かく制約を設ける
    //      ※詳しくは: https://qiita.com/superman9387/items/7441998138a8509537a4
    //
    // 今回は簡単な実装にしたいので 1. を採用
    // 日本郵便が提供する 郵便番号->住所 をしてくれるWebAPI
    private var postalNumber = 4418122
    private val URLbase = "https://zipcloud.ibsnet.co.jp/api/search?zipcode="
    private val URL = URLbase+postalNumber.toString()

    data class ZipResponse(
        var message : String ?= null,
        var status : String ?= null,
        var results : ArrayList<Address> = ArrayList()
    )

    data class Address(
        var address1 : String ?= null,
        var address2 : String ?= null,
        var address3 : String ?= null,
        var kana1 : String ?= null,
        var kana2 : String ?= null,
        var kana3 : String ?= null,
        var prefcode : String ?= null,
        var zipcode : String ?= null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView.text = textView.text.toString() +": "+ postalNumber
        callAPI()
    }

    private fun callAPI() = GlobalScope.launch(Dispatchers.Main){
        withContext(Dispatchers.Default){
            synGetResponse(URL)
        }.let{
            val result = Gson().fromJson(it, ZipResponse::class.java)
            val addrView = findViewById<TextView>(R.id.addrView)
            val message:String = result.results[0].address1 + " " +
                    result.results[0].address2 + " " +
                    result.results[0].address3
            addrView.text = message
        }
    }

    private fun synGetResponse(url: String): String? {
        return OkHttpClient()
            .newCall(Request.Builder().url(url).build())
            .execute()
            .body?.string()
    }
}
