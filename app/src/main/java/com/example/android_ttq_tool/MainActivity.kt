package com.example.android_ttq_tool

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)
		Genaral.setFullScreen(this)
		Genaral.hideTitle(this)
		setContentView(R.layout.activity_main)
		val backgroundImageView:ImageView = findViewById<ImageView>(R.id.mainActivityImageViewBackground)
		Genaral.setThemeAndBackground(this, backgroundImageView)
	}
	
	
}