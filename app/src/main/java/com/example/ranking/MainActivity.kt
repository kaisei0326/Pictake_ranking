package com.example.ranking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.github.kittinunf.fuel.*
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.coroutines.*
import com.github.kittinunf.result.Result
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    data class TestModel(
        var qty: Int
    )

    private val tag = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FuelManager.instance.apply {
            basePath = "http://192.168.43.230:8080"
            baseHeaders = mapOf("Device" to "Android")
            baseParams = listOf("key" to "value")
        }
        getRanking.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
                httpGet()
            }
        }

    }

    private suspend fun httpGet() {
        val (request, response, result) = "http://192.168.43.230:8080/ranking/qty".httpGet().awaitStringResponseResult()
        Log.d(tag, response.toString())
        Log.d(tag, request.toString())
        update(result)
    }

    private fun <T : Any> update(result: Result<T, FuelError>) {
        result.fold(success = {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val TestModel = moshi.adapter(TestModel::class.java).fromJson(it.toString())
            mainResultText.append("qry -> ${TestModel?.qty}")
        }, failure = {
            mainResultText.append(String(it.errorData))
        })
    }
}