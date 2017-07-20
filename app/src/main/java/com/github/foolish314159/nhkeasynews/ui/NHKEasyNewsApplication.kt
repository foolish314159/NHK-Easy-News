package com.github.foolish314159.nhkeasynews.ui

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.orm.SchemaGenerator
import com.orm.SugarContext
import com.orm.SugarDb

class NHKEasyNewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Fresco.initialize(this)
        SugarContext.init(this)

        val schemaGen = SchemaGenerator(this)
        schemaGen.createDatabase(SugarDb(this).db)
    }

    override fun onTerminate() {
        super.onTerminate()
        SugarContext.terminate()
    }
}