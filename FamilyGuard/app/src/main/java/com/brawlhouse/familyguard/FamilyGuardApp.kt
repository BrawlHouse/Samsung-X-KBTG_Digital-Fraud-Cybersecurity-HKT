    package com.brawlhouse.familyguard

    import android.app.Application
    import com.google.firebase.FirebaseApp

    class FamilyGuardApp : Application() {
        override fun onCreate() {
            super.onCreate()
            // บรรทัดนี้จะอ่านค่าจาก google-services.json ที่คุณเพิ่งวางลงไป
            FirebaseApp.initializeApp(this)
        }
    }