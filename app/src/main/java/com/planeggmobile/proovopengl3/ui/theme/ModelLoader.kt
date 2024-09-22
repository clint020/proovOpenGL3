package com.planeggmobile.proovopengl3.ui.theme

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun loadModelAsync(context: Context, objResId: Int, callback: () -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        // Taustal Java mudeli laadimine
        val objModel = ObjModel(context, objResId)
        objModel.loadObj(context, objResId)

        // Kui taustatöö on lõpetatud, kutsutakse tagasi UI lõime
        withContext(Dispatchers.Main) {
            callback()
        }
    }
}
