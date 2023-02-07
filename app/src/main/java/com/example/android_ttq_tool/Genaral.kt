package com.example.android_ttq_tool

import android.app.Activity
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import java.util.*

class Genaral {
	companion object Static{
		 fun setThemeAndBackground(activity: Activity, background: ImageView){
			val rightNow = Calendar.getInstance()
			val currentHourIn24Format: Int =rightNow.get(Calendar.HOUR_OF_DAY)
			if(currentHourIn24Format >= 18 || currentHourIn24Format <= 6 ){
				activity.setTheme(R.style.AppThemeFullscreenNight)
				background.setBackgroundResource(R.drawable.background_original_night)
			}else if(currentHourIn24Format <= 12 ){
				activity.setTheme(R.style.AppThemeFullscreenMorning)
				background.setBackgroundResource(R.drawable.background_original_morning)
			}else if(currentHourIn24Format >= 12 ) {
				activity.setTheme(R.style.AppThemeFullscreenAfternoon)
				background.setBackgroundResource(R.drawable.background_original_afternoon)
			}else{
				//default
				activity.setTheme(R.style.AppThemeFullscreen)
				background.setBackgroundResource(R.drawable.background5)
			}
		}
		fun setFullScreen(activity: Activity){
			activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
			activity.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}
	
}