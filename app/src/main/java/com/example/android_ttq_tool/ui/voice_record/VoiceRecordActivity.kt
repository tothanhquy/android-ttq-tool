package com.example.android_ttq_tool.ui.voice_record

import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.MemoryFile
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.android_ttq_tool.Genaral
import com.example.android_ttq_tool.R
import java.io.File
import java.io.IOException
import java.nio.file.Files


const val NAVBAR_ITEM_CHANGE_ALPHA_SHOW:Float = 1.0f
const val NAVBAR_ITEM_CHANGE_ALPHA_HIDE:Float = 0.3f
const val NAVBAR_ITEM_CHANGE_MARGIN_BOTTOM_SHOW:Int = 10
const val NAVBAR_ITEM_CHANGE_MARGIN_BOTTOM_HIDE:Int = 0
const val NAVBAR_ITEM_CHANGE_DURATION:Int = 300
const val LAYOUT_CHANGE_ALPHA_SHOW:Float = 1.0f
const val LAYOUT_CHANGE_ALPHA_HIDE:Float = 0f
const val LAYOUT_CHANGE_SCALE_SHOW:Float = 1f
const val LAYOUT_CHANGE_SCALE_HIDE:Float = 0.1f

class VoiceRecordActivity : AppCompatActivity() {
	
	private var layoutNow:String = ""
	private var createScreenStatus:String = ""
	private lateinit var createScreenStart:ViewGroup
	private lateinit  var createScreenPause:ViewGroup
	private lateinit  var createScreenContinue:ViewGroup
	private lateinit  var createScreenStop:ViewGroup
	private lateinit  var createScreenTime:View
	
	private  var recordTimeStart:Long = 0
	private  var recordTimeMinusPause:Long = 0
	private  var recordTimePause:Long = 0
	
	private var mRecorder:MediaRecorder? = MediaRecorder()
	
	var REQUEST_AUDIO_PERMISSION_CODE = 1
	
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)
		Genaral.setFullScreen(this)
//		Genaral.hideTitle(this)
		setContentView(R.layout.activity_voice_record)
		val backgroundColor:Int = getColor(R.color.background3)
		Genaral.setTitleBarAndNavigationBar(
			this,
			backgroundColor,
			R.string.title_activity_voice_record
		)
		
		createScreenStart = findViewById(R.id.voiceRecordCreateScreenStart)
		createScreenPause = findViewById(R.id.voiceRecordCreateScreenPause)
		createScreenContinue = findViewById(R.id.voiceRecordCreateScreenContinue)
		createScreenStop = findViewById(R.id.voiceRecordCreateScreenStop)
		createScreenTime = findViewById(R.id.voiceRecordCreateScreenTime)
		
		changeCreateScreenLayout("readly")
		changeNavbarItem(findViewById(R.id.voiceRecordNavbarItemMic))
		
		if(!CheckPermissions())RequestPermissions()
	}
	
	override fun onDestroy() {
		super.onDestroy()
		//free memories
		mRecorder?.release()
	}
	
	override fun  onOptionsItemSelected(item: MenuItem):Boolean {
		val id:Int = item.getItemId();
		
		if ( id == android.R.id.home ) {
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private fun changeNavbarItemWithAnimation(view : View,isShow:Boolean=true){

		val alphaDifference:Float
		val alphaStart:Float
		val marginBottomDifference:Int
		val marginBottomStart:Int
		if(isShow===true){
			alphaStart = NAVBAR_ITEM_CHANGE_ALPHA_HIDE
			alphaDifference =NAVBAR_ITEM_CHANGE_ALPHA_SHOW - NAVBAR_ITEM_CHANGE_ALPHA_HIDE
			marginBottomStart = NAVBAR_ITEM_CHANGE_MARGIN_BOTTOM_HIDE
			marginBottomDifference = NAVBAR_ITEM_CHANGE_MARGIN_BOTTOM_SHOW - NAVBAR_ITEM_CHANGE_MARGIN_BOTTOM_HIDE
		}else{
			alphaStart = NAVBAR_ITEM_CHANGE_ALPHA_SHOW
			alphaDifference =-1*(NAVBAR_ITEM_CHANGE_ALPHA_SHOW - NAVBAR_ITEM_CHANGE_ALPHA_HIDE)
			marginBottomStart = NAVBAR_ITEM_CHANGE_MARGIN_BOTTOM_SHOW
			marginBottomDifference = -1*(NAVBAR_ITEM_CHANGE_MARGIN_BOTTOM_SHOW - NAVBAR_ITEM_CHANGE_MARGIN_BOTTOM_HIDE)
		}
		
		val a : Animation = object : Animation() {
			override fun applyTransformation(
				interpolatedTime : Float ,
				t : Transformation?
			) {
				view.alpha = alphaStart+alphaDifference*interpolatedTime
				val param:ViewGroup.MarginLayoutParams = view.layoutParams as MarginLayoutParams
				param.setMargins(0,0,0,(Genaral.convertDpToPx(applicationContext,marginBottomStart+marginBottomDifference.toFloat()*interpolatedTime)).toInt())
				view.layoutParams = param
			}
		}
		a.duration = NAVBAR_ITEM_CHANGE_DURATION.toLong() // in ms
		
		view.startAnimation(a)
	}
	
	private fun changeMainLayoutWithAnimation(layout : ViewGroup,isShow:Boolean=true){
		if(isShow===true){
			layout.animate().alpha(LAYOUT_CHANGE_ALPHA_SHOW).scaleX(LAYOUT_CHANGE_SCALE_SHOW).scaleY(LAYOUT_CHANGE_SCALE_SHOW).duration = NAVBAR_ITEM_CHANGE_DURATION.toLong()
		}else{
			layout.animate().alpha(LAYOUT_CHANGE_ALPHA_HIDE).scaleX(LAYOUT_CHANGE_SCALE_HIDE).scaleY(LAYOUT_CHANGE_SCALE_HIDE).duration = NAVBAR_ITEM_CHANGE_DURATION.toLong()
		}
	}
	
	fun changeNavbarItem(view: View){
		val micView:ImageView = findViewById(R.id.voiceRecordNavbarItemMic);
		val storeView:ImageView = findViewById(R.id.voiceRecordNavbarItemStore);
		val createLayout:ViewGroup = findViewById(R.id.voiceRecordCreateScreen);
		val listLayout:ViewGroup = findViewById(R.id.voiceRecordListScreen);
		
		if(view.id===R.id.voiceRecordNavbarItemMic){
			if(layoutNow==="createLayout")return
			changeNavbarItemWithAnimation(micView,true)
			changeNavbarItemWithAnimation(storeView,false)
//			change layout
			
			changeMainLayoutWithAnimation(createLayout,true)
			changeMainLayoutWithAnimation(listLayout,false)
			layoutNow = "createLayout"
		}else if(view.id===R.id.voiceRecordNavbarItemStore){
			if(layoutNow==="listLayout")return
			changeNavbarItemWithAnimation(storeView,true)
			changeNavbarItemWithAnimation(micView,false)
//			change layout
			changeMainLayoutWithAnimation(listLayout,true)
			changeMainLayoutWithAnimation(createLayout,false)
			layoutNow = "listLayout"
		}
	}
	
	private fun changeCreateScreenButtonWithAnimation(view : View,isShow:Boolean=true){
		if(isShow===true){
			view.animate().alpha(LAYOUT_CHANGE_ALPHA_SHOW).scaleX(LAYOUT_CHANGE_SCALE_SHOW).scaleY(LAYOUT_CHANGE_SCALE_SHOW).duration = NAVBAR_ITEM_CHANGE_DURATION.toLong()
			view.isClickable = true
			view.elevation = 2f
		}else{
			view.animate().alpha(LAYOUT_CHANGE_ALPHA_HIDE).scaleX(LAYOUT_CHANGE_SCALE_HIDE).scaleY(LAYOUT_CHANGE_SCALE_HIDE).duration = NAVBAR_ITEM_CHANGE_DURATION.toLong()
			view.isClickable = false
			view.elevation = 1f
		}
	}
	
	private fun changeCreateScreenLayout(nextStatus:String){
		if(createScreenStatus==""&&nextStatus=="readly"){
			changeCreateScreenButtonWithAnimation(createScreenStart,true)
			changeCreateScreenButtonWithAnimation(createScreenPause,false)
			changeCreateScreenButtonWithAnimation(createScreenStop,false)
			changeCreateScreenButtonWithAnimation(createScreenTime,false)
		}else if(createScreenStatus=="readly"){
			if(nextStatus=="recording"){
				changeCreateScreenButtonWithAnimation(createScreenStart,false)
				changeCreateScreenButtonWithAnimation(createScreenPause,true)
				changeCreateScreenButtonWithAnimation(createScreenStop,true)
				changeCreateScreenButtonWithAnimation(createScreenTime,true)
			}else return
		}else if(createScreenStatus=="recording"){
			if(nextStatus=="pause"){
				changeCreateScreenButtonWithAnimation(createScreenPause,false)
				changeCreateScreenButtonWithAnimation(createScreenContinue,true)
			}else if(nextStatus=="readly"){
				//stop recording
				changeCreateScreenButtonWithAnimation(createScreenStart,true)
				changeCreateScreenButtonWithAnimation(createScreenPause,false)
				changeCreateScreenButtonWithAnimation(createScreenStop,false)
				changeCreateScreenButtonWithAnimation(createScreenTime,false)
			}else return
		}else  if(createScreenStatus=="pause"){
			if(nextStatus=="recording"){
				changeCreateScreenButtonWithAnimation(createScreenPause,true)
				changeCreateScreenButtonWithAnimation(createScreenContinue,false)
			}else if(nextStatus=="readly"){
				//stop recording
				changeCreateScreenButtonWithAnimation(createScreenStart,true)
				changeCreateScreenButtonWithAnimation(createScreenContinue,false)
				changeCreateScreenButtonWithAnimation(createScreenStop,false)
				changeCreateScreenButtonWithAnimation(createScreenTime,false)
			}else return
		}
		createScreenStatus = nextStatus
	}
	
	fun changeCreateScreenStatus(view:View){
		if(view.id==R.id.voiceRecordCreateScreenStart){
//			start record
			println("start")
			if(createScreenStatus=="readly"){
				startRecording()
			}
		}else if(view.id==R.id.voiceRecordCreateScreenPause){
//			pause record
			println("pause")
			if(createScreenStatus=="recording"){
				pauseRecording()
			}
		}else if(view.id==R.id.voiceRecordCreateScreenContinue){
//			continue record
			println("continue")
			if(createScreenStatus=="pause"){
				continueRecording()
			}
		}else if(view.id==R.id.voiceRecordCreateScreenStop){
//			finish record
			println("stop")
			stopRecording()
		}
	}
	
	
	
//	fun onRequestPermissionsResult(
//		requestCode : Int ,
//		permissions : Array<String?>? ,
//		grantResults : IntArray
//	) {
//		lateinit var permissionsOut:Array<out String>
//		super.onRequestPermissionsResult(requestCode , permissionsOut , grantResults)
//
//		// this method is called when user will
//		// grant the permission for audio recording.
//		when (requestCode) {
//			REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.size > 0) {
//				val permissionToRecord =
//					grantResults[0] == PackageManager.PERMISSION_GRANTED
//				val permissionToStore =
//					grantResults[1] == PackageManager.PERMISSION_GRANTED
//				if (permissionToRecord && permissionToStore) {
//					Toast.makeText(
//						applicationContext ,
//						"Permission Granted" ,
//						Toast.LENGTH_LONG
//					).show()
//				} else {
//					Toast.makeText(
//						applicationContext ,
//						"Permission Denied" ,
//						Toast.LENGTH_LONG
//					).show()
//				}
//			}
//		}
//	}
	
	fun CheckPermissions() : Boolean {
		// this method is used to check permission
		val result = ContextCompat.checkSelfPermission(
			applicationContext ,
			WRITE_EXTERNAL_STORAGE
		)
		val result1 = ContextCompat.checkSelfPermission(
			applicationContext , 
			RECORD_AUDIO
		)
		return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
	}
	
	private fun showMessageOKCancel(
		message : String ,
		okListener : DialogInterface.OnClickListener
	) {
		AlertDialog.Builder(this)
			.setMessage(message)
			.setPositiveButton("OK" , okListener)
			.setNegativeButton("Cancel" , null)
			.create()
			.show()
	}
	
	private fun RequestPermissions() {
		showMessageOKCancel(
			"We need to allow access to Record Audio and Write Storage"
		) { dialog:DialogInterface  , which:Int->
			ActivityCompat.requestPermissions(
				this,
				arrayOf<String>(
					WRITE_EXTERNAL_STORAGE ,
					RECORD_AUDIO
				) ,
				REQUEST_AUDIO_PERMISSION_CODE
			)
		}
	}
	
	
	private fun startRecording() {
		if (CheckPermissions()) {
			try {
				var mFileName =	Environment.getExternalStorageDirectory().absolutePath + "/ttq-tool/VoiceRecord/"
				var directory: File = File(mFileName)
				if (! directory.exists()){
					directory.mkdirs();
				}
				mFileName += Genaral.getTimeStringNow()+".AudioRecording.3gp"
				println(mFileName)
				mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
				mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
				mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
				mRecorder?.setOutputFile(mFileName)
				mRecorder?.prepare()
				mRecorder?.start()
				
				changeCreateScreenLayout("recording")
				
				recordTimeStart = Genaral.getMillisecondNow()
				recordTimeMinusPause = 0
				recordTimePause = 0
				startThreadTime()
			} catch (e : Exception) {
				Genaral.showError(this,"Error!","Record failed because system error.")
				println(e.message+e.cause)
			}
		} else {
			RequestPermissions()
		}
	}
	
	private fun pauseRecording() {
		try{
			mRecorder?.pause()
			recordTimePause = Genaral.getMillisecondNow()
			changeCreateScreenLayout("pause")
		} catch (e : Exception) {
			Genaral.showError(this,"Error!","Record failed because system error.")
			println(e.message+e.cause)
		}
	}
	private fun continueRecording() {
		try{
			mRecorder?.resume()
			recordTimeMinusPause+=Genaral.getMillisecondNow()-recordTimePause
			recordTimePause = 0
			startThreadTime()
			changeCreateScreenLayout("recording")
		} catch (e : Exception) {
			Genaral.showError(this,"Error!","Record failed because system error.")
			println(e.message+e.cause)
		}
	}
	private fun stopRecording() {
		try{
			mRecorder?.stop()
			mRecorder?.reset()
//			mRecorder?.release()
			recordTimeStart = 0
			changeCreateScreenLayout("readly")
			//alert finish
			
		} catch (e : Exception) {
			Genaral.showError(this,"Error!","Record failed because system error.")
			println(e.message)
		}
	}
	private fun threadTimeLoopFunc(){
		var time:TextView = createScreenTime as TextView
		time.text =
			Genaral.convertMillisecondToTimeString(
				Genaral.getMillisecondNow()-recordTimeStart-recordTimeMinusPause)
	}
	
	private fun startThreadTime(){
		threadTimeLoopFunc()
		Thread(Runnable {
			while(true) {
				if (recordTimePause !== 0.toLong() || recordTimeStart === 0.toLong()) {
					//stop thread
					break
				} else {
					Thread.sleep(250)
					//work with other thread
					runOnUiThread(Runnable {
						threadTimeLoopFunc()
					})
				}
				
			}
		}).start()
	}
//
//	fun pausePlaying() {
//		// this method will release the media player
//		// class and pause the playing of our recorded audio.
//		mPlayer.release()
//		mPlayer = null
//		stopTV.setBackgroundColor(resources.getColor(R.color.gray))
//		startTV.setBackgroundColor(resources.getColor(R.color.purple_200))
//		playTV.setBackgroundColor(resources.getColor(R.color.purple_200))
//		stopplayTV.setBackgroundColor(resources.getColor(R.color.gray))
//		statusTV.setText("Recording Play Stopped")
//	}

//	fun playAudio() {
//		stopTV.setBackgroundColor(resources.getColor(R.color.gray))
//		startTV.setBackgroundColor(resources.getColor(R.color.purple_200))
//		playTV.setBackgroundColor(resources.getColor(R.color.gray))
//		stopplayTV.setBackgroundColor(resources.getColor(R.color.purple_200))
//
//		// for playing our recorded audio
//		// we are using media player class.
//		mPlayer = MediaPlayer()
//		try {
//			// below method is used to set the
//			// data source which will be our file name
//			mPlayer.setDataSource(mFileName)
//
//			// below method will prepare our media player
//			mPlayer.prepare()
//
//			// below method will start our media player.
//			mPlayer.start()
//			statusTV.setText("Recording Started Playing")
//		} catch (e : IOException) {
//			Log.e("TAG" , "prepare() failed")
//		}
//	}
//
}