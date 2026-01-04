package com.riskguard.frontend.ui

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.riskguard.frontend.R
import com.riskguard.frontend.monitor.CallMonitorService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            startSystem()
        }
    }

    private fun startSystem() {
        // เริ่ม Service mock call session
        CallMonitorService.start(this)
    }
}
