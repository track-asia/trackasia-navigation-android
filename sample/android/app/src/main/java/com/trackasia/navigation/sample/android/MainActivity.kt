package com.trackasia.navigation.sample.android

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.trackasia.android.TrackAsia
import com.trackasia.navigation.sample.android.core.CoreOnlyFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        //TODO: move this to application calls
        TrackAsia.getInstance(applicationContext)

        supportFragmentManager.beginTransaction()
            .add(R.id.flFragmentContainer, CoreOnlyFragment())
            .commit()
    }
}