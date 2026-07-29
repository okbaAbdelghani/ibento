package com.okbatech.smartevents

import android.app.Application
import com.okbatech.smartevents.core.database.DatabaseSeeder
import com.okbatech.smartevents.core.xmpp.XmppManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SmartEventsApp : Application() {

    @Inject lateinit var databaseSeeder: DatabaseSeeder
    @Inject lateinit var xmppManager: XmppManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch { databaseSeeder.seedIfNeeded() }
        xmppManager.start(applicationScope)
    }
}
