package com.m.playwithcoroutines

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    val TIME_OUT = 1500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //callAPIsParallel()
        //callAPIsSequentially()
        //callAPIsParallelAsyncAwait()
        // callAPIsSequentiallyAsyncAwait()
        //callAPTimeOutOrNull()
        callAPTimeOut()

    }

    private fun callAPTimeOut() {
        CoroutineScope(IO).launch {
            //here we can use withContext to sequentially
            withTimeout(TIME_OUT) {

                val result =
                    launch { getResultFromApiOne() }
                val result2 =
                    launch { getResultFromApiTwo() }

                result.invokeOnCompletion {
                    println("debug 1 " + it.toString())
                    println("debug 1 cancel ${result.isCancelled}")
                }
                result2.invokeOnCompletion {
                    println("debug 2 " + it.toString())
                    println("debug 2 cancel ${result2.isCancelled}")
                }
            }
        }
    }

    private fun callAPTimeOutOrNull() {
        CoroutineScope(IO).launch {
            //here we can use withContext to sequentially
            val job = withTimeoutOrNull(TIME_OUT) {
                val result =
                    withContext(IO) { getResultFromApiOne() }
                val result2 =
                    withContext(IO) { getResultFromApiTwo() }
                setTextOnMainThread(result)
                setTextOnMainThread(result2)
            }
            if (job == null) {

                println("debug ERROR")
            }
        }
    }

    private fun callAPIsSequentiallyAsyncAwait() {
        CoroutineScope(IO).launch {
            //here we can use withContext to sequentially
            val time = measureTimeMillis {
                val result = async { getResultFromApiOne() }.await()
                val result2 = async { getResultFromApiTwo() }.await()
                setTextOnMainThread(result)
                setTextOnMainThread(result2)
            }
            println("Total time at $time")//time here will be sum of two times 2900 + some millSecond + time take to map data into UI
        }
    }

    private fun callAPIsParallelAsyncAwait() {
        CoroutineScope(IO).launch {
            val time = measureTimeMillis {
                val result = async { getResultFromApiOne() }
                val result2 = async { getResultFromApiTwo() }
                setTextOnMainThread(result.await())
                setTextOnMainThread(result2.await())
            }
            println("Total time at $time") //time here will be max time between 2 jobs 1900 + some millSeconds + time take to map data into UI
        }
    }

    private fun callAPIsParallel() {
        CoroutineScope(IO).launch {
            //here i use launch to make it run in parallel if i not use it it will execute sequentially
            val time = measureTimeMillis {
                launch {
                    val time1 = measureTimeMillis {
                        setTextOnMainThread(getResultFromApiOne())
                    }
                }
                launch {
                    val time2 = measureTimeMillis {
                        setTextOnMainThread(getResultFromApiTwo())
                    }
                }
            }
            println("Total time at $time") //time here will be max time between 2 jobs + some millSeconds + time take to map data into UI
        }
    }

    private fun callAPIsSequentially() {
        CoroutineScope(IO).launch {
            val time = measureTimeMillis {
                val res1 = getResultFromApiOne()
                val res2 = getResultFromApiTwo()
                setTextOnMainThread(res2)
            }
            println("Total time is $time") //time here will be sum of two times 2900 + some millSecond + time take to map data into UI
        }
    }

    private suspend fun getResultFromApiOne(): String {
        delay(1000)
        return "{Result One}"
    }

    private suspend fun getResultFromApiTwo(): String {
        delay(1900) //not block thread it suspend coroutine
        return "{Result Two}"
    }

    private suspend fun setTextOnMainThread(newText: String) {
        withContext(Main) {
            //here i should set text to Text view in main thread to not crash
            text_view.text = newText
        }
    }
}
