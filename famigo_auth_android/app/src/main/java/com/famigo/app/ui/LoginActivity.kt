
package com.famigo.app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.famigo.app.databinding.ActivityLoginBinding

class LoginActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)
    }
}
