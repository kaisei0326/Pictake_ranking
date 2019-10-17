package com.example.ranking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.github.kittinunf.fuel.*
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.coroutines.*
import com.github.kittinunf.result.Result
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    data class TestModel(
        val qty: Int
    )

    data class Top(
        val profile: Profile,
        val rank: Int
    )

    data class Profile(
        val name: String,
//        val twitterId: String,
        val score: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FuelManager.instance.apply {
            basePath = "http://192.168.43.230:8080"
            baseHeaders = mapOf("Device" to "Android")
            baseParams = listOf("key" to "value")
        }

        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            showRanking()
        }
    }


    private suspend fun showRanking() {
        val (_, response, result) = Fuel.get("/ranking/top").awaitStringResponseResult()
        Log.d(MainActivity::class.java.simpleName, response.toString())
        update(result)
    }

    private fun <T : Any> update(result: Result<T, FuelError>) {
        result.fold(success = {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val listMyData = Types.newParameterizedType(List::class.java, Top::class.java)
            val jsonAdapter: JsonAdapter<List<Top>> = moshi.adapter(listMyData)
            val Top = jsonAdapter.fromJson(it.toString())
           Top!!.forEach{ element -> mainResultText.append("${element.rank}位 name: ${element.profile.name} score: ${element.profile.score}\n") }
        }, failure = {
            mainResultText.append(String(it.errorData))
        })
    }
}