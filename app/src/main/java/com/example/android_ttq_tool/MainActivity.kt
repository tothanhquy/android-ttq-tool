package com.example.android_ttq_tool

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)
		Genaral.setFullScreen(this)
		val backgroundImageView:ImageView = findViewById<ImageView>(R.id.mainActivityImageViewBackground)
		Genaral.setThemeAndBackground(this, backgroundImageView)
		setContentView(R.layout.activity_main)
	}
	
	
}