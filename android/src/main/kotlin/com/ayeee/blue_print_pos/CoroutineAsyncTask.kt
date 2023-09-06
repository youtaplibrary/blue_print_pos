package com.ayeee.blue_print_pos

import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.Executors

abstract class CoroutinesAsyncTask<Params, Progress, Result>(private val taskName: String) {

    companion object {
        private var threadPoolExecutor: CoroutineDispatcher? = null
    }

    private var preJob: Job? = null
    private var bgJob: Deferred<Result>? = null
    abstract fun doInBackground(vararg printersData: Params?): Result
    open fun onProgressUpdate(vararg values: Progress?) {}
    open fun onPostExecute(result: Result) {}
    open fun onPreExecute() {}
    open fun onCancelled(result: Result?) {}
    private var isCancelled = false

    /**
     * Executes background task parallel with other background tasks in the queue using
     * default thread pool
     */
    fun execute(vararg params: Params?) {
        execute(Dispatchers.Default, *params)
    }

    /**
     * Executes background tasks sequentially with other background tasks in the queue using
     * single thread executor @Executors.newSingleThreadExecutor().
     */
    fun executeOnExecutor(vararg params: Params?) {
        if (threadPoolExecutor == null) {
            threadPoolExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        }
        execute(threadPoolExecutor!!, *params)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun execute(dispatcher: CoroutineDispatcher, vararg params: Params?) {

        // it can be used to setup UI - it should have access to Main Thread
        CoroutineScope(Dispatchers.IO).launch {
            preJob = launch(Dispatchers.IO) {
                printLog("$taskName onPreExecute started")
                onPreExecute()
                printLog("$taskName onPreExecute finished")
                bgJob = async(dispatcher) {
                    printLog("$taskName doInBackground started")
                    doInBackground(*params)
                }
            }
            preJob!!.join()
            if (!isCancelled) {
                withContext(Dispatchers.IO) {
                    onPostExecute(bgJob!!.await())
                    printLog("$taskName doInBackground finished")
                }
            }
        }
    }

    fun cancel(mayInterruptIfRunning: Boolean) {
        if (preJob == null || bgJob == null) {
            printLog("$taskName has already been cancelled/finished/not yet started.")
            return
        }
        if (mayInterruptIfRunning || (!preJob!!.isActive && !bgJob!!.isActive)) {
            isCancelled = true
            if (bgJob!!.isCompleted) {
                CoroutineScope(Dispatchers.IO).launch {
                    onCancelled(bgJob!!.await())
                }
            }
            preJob?.cancel(CancellationException("PreExecute: Coroutine Task cancelled"))
            bgJob?.cancel(CancellationException("doInBackground: Coroutine Task cancelled"))
            printLog("$taskName has been cancelled.")
        }
    }

    fun publishProgress(vararg progress: Progress) {
        //need to update main thread
        CoroutineScope(Dispatchers.IO).launch {
            if (!isCancelled) {
                onProgressUpdate(*progress)
            }
        }
    }

    private fun printLog(message: String) {
        Log.d("CoroutinesAsyncTask", message)
    }
}