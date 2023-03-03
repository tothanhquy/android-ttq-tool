package com.example.android_ttq_tool

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface.OnClickListener
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
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
	
		fun createBackgroundShapeRectangle(bgColor:Int, borderRadius:Float, borderWidth:Int, borderColor:Int):GradientDrawable{
			val shape = GradientDrawable()
			shape.shape = GradientDrawable.RECTANGLE
			shape.cornerRadii = FloatArray(8){borderRadius}
			shape.setColor(bgColor)
			shape.setStroke(borderWidth , borderColor)
			return shape
		}
		fun convertDpToPx( context: Context,dp:Float):Float{
			return context.resources.displayMetrics.density*dp
		}
		
		fun setupActivityTitleBar(activity: AppCompatActivity){
		
		}
		
		fun setTitleBarAndNavigationBar(activity: AppCompatActivity, backgroundScreenColor:Int, titleString:Int){
//			set title bar
			activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
			activity.supportActionBar?.setTitle(titleString)
//		          set background color title
			val backgroundTitleColor:Int = Color.argb((Color.alpha(backgroundScreenColor)*0.7).toInt(),
				Color.red(backgroundScreenColor),
				Color.green(backgroundScreenColor),
				Color.blue(backgroundScreenColor))
			activity.supportActionBar?.setBackgroundDrawable(ColorDrawable(backgroundTitleColor))
//		          set window navigation bar color
			activity.window.navigationBarColor = backgroundTitleColor
		}
		
		fun createBackgroundOpacity(color:Int, alpha:Float):Int{
			return Color.argb((Color.alpha(color)*alpha).toInt(),
				Color.red(color),
				Color.green(color),
				Color.blue(color))
		}
		
		fun getTimeStringNow():String{
			val sdf =	SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
			return sdf.format(Date()).replace(' ','_')
		}
		
		fun showError(context:Context,title:String,content:String,listener : OnClickListener?=null){
			var builder:AlertDialog.Builder = AlertDialog.Builder(context)
			builder
				.setTitle(title)
				.setMessage(content)
				.setIcon(R.drawable.warning_icon)
				.setPositiveButton(android.R.string.ok,listener)
			var dialog:AlertDialog = builder.create()
			dialog.show()
			
		}
		fun showNotificationDialog(context:Context,title:String,content:String,listener : OnClickListener?=null){
			var builder:AlertDialog.Builder = AlertDialog.Builder(context)
			builder
				.setTitle(title)
				.setMessage(content)
				.setIcon(R.drawable.notification_icon)
				.setPositiveButton(android.R.string.ok,listener)
			var dialog:AlertDialog = builder.create()
			dialog.show()
		}
		fun showAskDialog(context:Context,title:String,content:String, listener: OnClickListener){
			var builder:AlertDialog.Builder = AlertDialog.Builder(context)
			builder
				.setTitle(title)
				.setMessage(content)
				.setIcon(R.drawable.question_icon)
				.setPositiveButton(android.R.string.yes,listener)
				.setOnCancelListener(null)
				.setNegativeButton(android.R.string.no, null)
			var dialog:AlertDialog = builder.create()
			dialog.show()
		}
		fun getMillisecondNow():Long{
			return System.currentTimeMillis()
		}
		fun convertMillisecondToTimeString(millisecond:Long):String{
//			println(millisecond)
			var res = ""
			val zero:Long = 0
			var milli:Long = millisecond
			
			if(milli/(1000*60*60)!==zero){
				//hour
				res+=""+(milli/(1000*60*60))+":"
				milli%=(1000*60*60)
			}
			//minus
			val minus = (milli/(1000*60))
			res+=""+(if (minus<10) "0"+minus else minus)
			milli%=(1000*60)
			//sec
			val sec = (milli/(1000))
			res+=":"+(if (sec<10) "0"+sec else sec)
			milli%=1000
			//millisec
//			milli/=100
//			res+="."+milli
			return res
		}
		
		fun createGetStringDialog(
			context:Context,title:String,
			defaultInput:String,
			funcHandel:(input:String?)->Boolean,//return true then dismiss
		):Dialog{
			var dialog = Dialog(context,android.R.style.Theme_Black_NoTitleBar_Fullscreen)
			
			dialog.setContentView(R.layout.get_string_dialog)
			dialog.findViewById<TextView>(R.id.get_string_dialog_title).text = title
			dialog.findViewById<EditText>(R.id.get_string_dialog_input).setText(defaultInput)
			dialog.findViewById<Button>(R.id.get_string_dialog_bt_default).setOnClickListener {
				if(funcHandel(null))
					dialog.dismiss()
			}
			dialog.findViewById<Button>(R.id.get_string_dialog_bt_ok).setOnClickListener {
				if(funcHandel(dialog.findViewById<EditText>(R.id.get_string_dialog_input).text.toString()))
					dialog.dismiss()
			}
			return dialog
		}
		
		fun vibrate(context:Context){
			try {
				val vibrator =
					context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
				if (Build.VERSION.SDK_INT >= 26) {
					vibrator.vibrate(
						VibrationEffect.createOneShot(
							300 ,
							VibrationEffect.DEFAULT_AMPLITUDE
						)
					)
				} else {
					vibrator.vibrate(300)
				}
			}catch (e:Exception){
			
			}
		}
		fun getLoadingScreen(context : Context,parent:ViewGroup,bg:Int):View{
			var screen: View = LayoutInflater.from(context).inflate(R.layout.loading_layout, parent, false);
			screen.setBackgroundColor(createBackgroundOpacity(bg,0.5f))
			var img = screen.findViewById<ImageView>(R.id.loadingScreenImage)
			val animation : Animation = AnimationUtils.loadAnimation(
				context,
				R.anim.rotate_animation_infinite
			)
			img.startAnimation(animation)
			return screen
		}
	}
	
}