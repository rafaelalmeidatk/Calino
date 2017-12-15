package com.cpti.calino

import android.app.Application
import android.content.Context
import com.facebook.stetho.Stetho
import com.uphyca.stetho_realm.RealmInspectorModulesProvider
import io.realm.Realm

/**
 * Created by rafael on 06/12/17.
 */
class MyApplication : Application() {
    companion object {
        // Not a good pratice...
        private var context_: Context? = null

        fun getAppContext(): Context? {
            return context_
        }
    }

    override fun onCreate() {
        super.onCreate()
        context_ = this

        // Initialize Stetho
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build())

        // Initialize Realm
        Realm.init(this)
    }
}