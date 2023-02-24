package com.example.android_ttq_tool.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.android_ttq_tool.Genaral
import com.example.android_ttq_tool.R
import com.example.android_ttq_tool.ui.voice_record.VoiceRecordActivity


const val MAIN_SCREEN_CARD_BORDER_RADIUS: Float = 30f
const val MAIN_SCREEN_CARD_BORDER_WIDTH: Float = 10f

class MainActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)
		Genaral.setFullScreen(this)
		Genaral.hideTitle(this)
		setContentView(R.layout.activity_main)
		val backgroundImageView:ImageView = findViewById<ImageView>(R.id.mainActivityImageViewBackground)
		Genaral.setThemeAndBackground(this, backgroundImageView)
		initBackgroundOfToolCard()
		changeMainScreenTitleToolBackground(
			ContextCompat.getColor(applicationContext,R.color.background3),
			Genaral.convertDpToPx(applicationContext,MAIN_SCREEN_CARD_BORDER_RADIUS),
			Genaral.convertDpToPx(applicationContext,MAIN_SCREEN_CARD_BORDER_WIDTH).toInt(),
			ContextCompat.getColor(applicationContext,R.color.white)
		)
	}
	
	fun initBackgroundOfToolCard(){
		findViewById<LinearLayout>(R.id.mainScreenMicroCard).setBackground(
			Genaral.createBackgroundShapeRectangle(
				ContextCompat.getColor(applicationContext,R.color.background3),
				Genaral.convertDpToPx(applicationContext,MAIN_SCREEN_CARD_BORDER_RADIUS),
				Genaral.convertDpToPx(applicationContext,MAIN_SCREEN_CARD_BORDER_WIDTH).toInt(),
				ContextCompat.getColor(applicationContext,R.color.white)
			)
		)

	}
	
	fun changeMainScreenTitleToolBackground(bgColor:Int, borderRadius:Float, borderWidth:Int, borderColor:Int){
		findViewById<LinearLayout>(R.id.mainScreenTitleTool).setBackground(
			Genaral.createBackgroundShapeRectangle(
				bgColor,
				borderRadius,
				borderWidth,
				borderColor
			)
		)
	}
	
	fun voiceRecordCardClick(view: View){
		val intent = Intent(applicationContext , VoiceRecordActivity::class.java)
		startActivity(intent)
	}
	
}