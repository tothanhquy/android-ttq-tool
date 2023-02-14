package com.example.android_ttq_tool

import android.app.Activity
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class Genaral {
	companion object Static{
		 fun setThemeAndBackground(activity: AppCompatActivity , background: ImageView){
			val rightNow = Calendar.getInstance()
			val currentHourIn24Format: Int =rightNow.get(Calendar.HOUR_OF_DAY)
			if(currentHourIn24Format >= 18 || currentHourIn24Format < 6 ){
				activity.setTheme(R.style.AppThemeFullscreenNight)
				background.setBackgroundResource(R.drawable.background_nigh_2_finaly)
				
			}else {
				activity.setTheme(R.style.AppThemeFullscreenDay)
				background.setBackgroundResource(R.drawable.background_day)
			
			}
		}
		fun setFullScreen(activity: AppCompatActivity){
			activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
			activity.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	
		fun hideTitle(activity: AppCompatActivity){
			activity.supportActionBar?.hide()
		}
	}
	
}