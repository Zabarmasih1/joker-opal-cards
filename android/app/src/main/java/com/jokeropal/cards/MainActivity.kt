package com.jokeropal.cards

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jokeropal.cards.core.di.ServiceLocator
import com.jokeropal.cards.core.navigation.Navigator
import com.jokeropal.cards.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.init(applicationContext)
        val inflated = ActivityMainBinding.inflate(layoutInflater)
        binding = inflated
        setContentView(inflated.root)
        if (savedInstanceState == null) {
            Navigator.showSplash(supportFragmentManager)
        }
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}
