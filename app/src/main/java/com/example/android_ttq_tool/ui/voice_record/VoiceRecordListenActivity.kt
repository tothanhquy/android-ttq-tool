package com.example.android_ttq_tool.ui.voice_record

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.example.android_ttq_tool.Genaral
import com.example.android_ttq_tool.R
import java.io.File
import java.io.FileInputStream


class VoiceRecordListenActivity : AppCompatActivity() {
	
	
	val VOICE_SPEED_LIST_SPINNER_SPEED:FloatArray = floatArrayOf(1.0f,0.25f,0.5f,2f,4f)
	val VOICE_SPEED_LIST_SPINNER_SEC:IntArray = intArrayOf(5,10,20,30,60)
	
	private var recordFilePath:String? = ""
	private var mediaPlayer:MediaPlayer? = MediaPlayer()
	private var spinnerSpeed:Spinner? = null
	private var spinnerSec:Spinner? = null
	private var spinnerSecValue:Int = 0
	private var mainImage:ImageView? = null
	private var mainImageAnimator:ObjectAnimator? = null
	private var btPlay:ImageView? = null
	private var btPause:ImageView? = null
	private var btStop:ImageView? = null
	private var recordTimeCount:Int? = 0
	private var recordSeekBar:SeekBar? = null
	private var statusPlayer:String? = ""
	private var currentPositionPlayer:Int = 0
	private var recordTimeSize : Int = 0
	private var timeRunning:TextView? = null
	private var exitTimeThread:Boolean = true
	
	@SuppressLint("MissingInflatedId")
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)
		Genaral.setFullScreen(this)
		setContentView(R.layout.activity_voice_record_listen)
		val backgroundColor:Int = getColor(R.color.background3)
		Genaral.setTitleBarAndNavigationBar(
			this,
			backgroundColor,
			R.string.title_activity_voice_record_listen
		)
		//get view
		spinnerSpeed = findViewById<Spinner>(R.id.voiceRecordListenSpinnerSpeed)
		spinnerSec = findViewById<Spinner>(R.id.voiceRecordListenSpinnerSec)
		mainImage =  findViewById<ImageView>(R.id.voiceRecordListenMainImageView)
		btPlay =  findViewById<ImageView>(R.id.voiceRecordListenPlayButton)
		btPause =  findViewById<ImageView>(R.id.voiceRecordListenPauseButton)
		btStop =  findViewById<ImageView>(R.id.voiceRecordListenStopButton)
		recordSeekBar = findViewById<SeekBar>(R.id.voiceRecordListenSeekBar)
		timeRunning = findViewById<TextView>(R.id.voiceRecordListenTimeRunning)
		
		//load loading animation screen
		var viewActivity = findViewById<ViewGroup>(R.id.voiceRecordActivity)
		var loadingLayout = Genaral.getLoadingScreen(this,viewActivity,backgroundColor)
		viewActivity.addView(loadingLayout)
		//get file path
		val intent: Intent = getIntent();
		var filePath:String? = intent.getStringExtra("FilePath");
		if(filePath==null||filePath==""){
			Genaral.showError(this,"Error!","File not exist.",fun(dia:DialogInterface,i:Int){finishWithResult(false)})
			//hide activity
//			findViewById<ScrollView>(R.id.voiceRecordMainScreen).visibility = View.GONE
		}else{
			recordFilePath = filePath
			try {
				setMediaPlayer()
				//init variable
				recordTimeSize = (File(recordFilePath).length()/1024).toInt()
				changeStatusButtons("ready")
				//set data layout
				setAllLabel()
				setSpinnerData()
				setMainImageAnimation()
				setSeekBarData()
				startThreadTime()
				//remove loading layout
				viewActivity.removeView(loadingLayout)
			}catch (e:Exception){
				Genaral.showError(this,"Error!","Can not open this file.",fun(dia:DialogInterface,i:Int){finishWithResult(false)})
				//hide activity
			
			
//				findViewById<ScrollView>(R.id.voiceRecordMainScreen).visibility = View.GONE
			
			}
		}
	}
	private fun setMediaPlayer(){
		mediaPlayer = MediaPlayer().apply {
			setAudioAttributes(
				AudioAttributes.Builder()
					.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
					.setUsage(AudioAttributes.USAGE_MEDIA)
					.build()
			)
			setDataSource(FileInputStream(File(recordFilePath)).fd)
			prepare()
		}
		mediaPlayer!!.setOnCompletionListener {
			stop(View(this))
		}
		mediaPlayer!!.seekTo(0)
	}
	override fun onDestroy() {
		super.onDestroy()
		exitTimeThread = true
		//free memories
		mediaPlayer?.release()
	}
	override fun  onOptionsItemSelected(item: MenuItem):Boolean {
		val id:Int = item.getItemId();
		
		if ( id == android.R.id.home ) {
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private fun finishWithResult(isDelete:Boolean){
		var resultIntent:Intent = Intent();
//		resultIntent.putExtra("isDelete",isDelete);  // put data that you want returned to activity A
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
	private fun setAllLabel(){
		val label = recordFilePath!!.substring(recordFilePath!!.lastIndexOf('/')+1,recordFilePath!!.length)
		val time = label!!.substring(0,label.indexOf('.')).replace('_',' ')
		val name = label.substring(label.indexOf('.')+1,label.lastIndexOf('.'))
		findViewById<TextView>(R.id.voiceRecordListenLabelName).text = name
		findViewById<TextView>(R.id.voiceRecordListenLabelTime).text = time
		findViewById<TextView>(R.id.voiceRecordListenFileSize).text = recordTimeSize.toString()+"kb"
		findViewById<TextView>(R.id.voiceRecordListenFilePath).text = recordFilePath
		findViewById<TextView>(R.id.voiceRecordListenTimeCount).text = Genaral.convertMillisecondToTimeString(mediaPlayer!!.duration.toLong())
	}
	
	private fun setSpinnerData(){
		setSpinnerSpeedData()
		setSpinnerSecData()
	}
	private fun setSpinnerSpeedData(){
		val arr = VOICE_SPEED_LIST_SPINNER_SPEED.map { it.toString() }
		val adapter = ArrayAdapter(
			this ,
			android.R.layout.simple_spinner_item , arr
		)
		spinnerSpeed!!.adapter = adapter
		spinnerSpeed!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
			override fun onNothingSelected(parent: AdapterView<*>?) {
			
			}
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				val isCurrentPlaying = mediaPlayer!!.isPlaying
				mediaPlayer !!.setPlaybackParams(
						mediaPlayer !!.getPlaybackParams()
							.setSpeed(
								VOICE_SPEED_LIST_SPINNER_SPEED.get(position)
							)
					);
				if(!isCurrentPlaying)mediaPlayer!!.pause()
			}
		}
	}
	private fun setSpinnerSecData(){
		spinnerSecValue = VOICE_SPEED_LIST_SPINNER_SEC.get(0)
		val arr = VOICE_SPEED_LIST_SPINNER_SEC.map { it.toString() }
		val adapter = ArrayAdapter(
			this ,
			android.R.layout.simple_spinner_item , arr
		)
		spinnerSec!!.adapter = adapter
		spinnerSec?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
			override fun onNothingSelected(parent: AdapterView<*>?) {
			
			}
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				spinnerSecValue = VOICE_SPEED_LIST_SPINNER_SEC.get(position)
			}
		}
	}
	private fun setSeekBarData(){
		recordSeekBar!!.max = mediaPlayer!!.duration
		recordSeekBar!!.progress = 0
		recordSeekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
			override fun onStopTrackingTouch(seekBar : SeekBar) {
				// TODO Auto-generated method stub
			}
			
			override fun onStartTrackingTouch(seekBar : SeekBar) {
				// TODO Auto-generated method stub
			}
			
			override fun onProgressChanged(
				seekBar : SeekBar ,
				progress : Int ,
				fromUser : Boolean
			) {
				// TODO Auto-generated method stub
				if(fromUser)mediaPlayer!!.seekTo(progress)
			}
		})
	}
	private fun setMainImageAnimation(){
		mainImageAnimator = ObjectAnimator.ofFloat(mainImage, View.ROTATION,0f,360f)
		mainImageAnimator!!.setDuration(5000)
		mainImageAnimator!!.setRepeatCount(Animation.INFINITE)
		mainImageAnimator!!.setInterpolator(LinearInterpolator())
//		mainImageAnimator!!.start()
//		mainImageAnimator!!.pause()
//		mainImageAnimator!!.resume()
	}
	private fun changeStatusButtons(status:String){
		when (status) {
			"ready" -> {
				showHideStatusButton(btPlay!!,true)
				showHideStatusButton(btPause!!,false)
				showHideStatusButton(btStop!!,false)
			}
			"running" -> {
				showHideStatusButton(btPlay!!,false)
				showHideStatusButton(btPause!!,true)
				showHideStatusButton(btStop!!,true)
			}
			"pause" -> {
				showHideStatusButton(btPlay!!,true)
				showHideStatusButton(btPause!!,false)
				showHideStatusButton(btStop!!,true)
			}
		}
		statusPlayer=status
	}
	private fun showHideStatusButton(view:ImageView, isShow:Boolean = true){
		if(isShow){
			view.alpha = 1f
			view.isEnabled = true
			view.isClickable = true
		}else{
			view.alpha = 0.3f
			view.isEnabled = false
			view.isClickable = false
		}
	}
	fun start(view:View){
		when(statusPlayer){
			"ready"->{
				mediaPlayer!!.start()
//				mediaPlayer!!.seekTo(0)
				mainImageAnimator!!.start()
				changeStatusButtons("running")
			}
			"pause"->{
				mediaPlayer!!.start()
				mediaPlayer!!.seekTo(currentPositionPlayer)
				mainImageAnimator!!.resume()
				changeStatusButtons("running")
			}
		}
		
	}
	fun pause(view:View){
		if(!mediaPlayer!!.isPlaying)return
		currentPositionPlayer = mediaPlayer!!.currentPosition
		mediaPlayer!!.pause()
		mainImageAnimator!!.pause()
		changeStatusButtons("pause")
	}
	fun stop(view:View){
		mediaPlayer!!.pause()
		mediaPlayer!!.seekTo(0)
//		mediaPlayer!!.stop()
		mainImageAnimator!!.pause()
		changeStatusButtons("ready")
	}
	fun nextPreviousButton(view:View){
		var nextPos:Int = 0
		when(view.id){
			R.id.voiceRecordListenNextButton->{
				nextPos= mediaPlayer!!.currentPosition + spinnerSecValue*1000
				if(nextPos>mediaPlayer!!.duration)nextPos=mediaPlayer!!.duration
				
			}
			R.id.voiceRecordListenPreviousButton->{
				nextPos= mediaPlayer!!.currentPosition - spinnerSecValue*1000
				if(nextPos<0)nextPos=0
			}
		}
		mediaPlayer!!.seekTo(nextPos)
	}
	private fun threadTimeRunningLoopFunc():Boolean{
		try{
			timeRunning!!.text =
				Genaral.convertMillisecondToTimeString(mediaPlayer!!.currentPosition.toLong())
			recordSeekBar!!.progress=mediaPlayer!!.currentPosition
			return true
		}catch (e:Exception){
			return false
		}
	}
	
	private fun startThreadTime(){
		exitTimeThread = false
		threadTimeRunningLoopFunc()
		Thread(Runnable {
			while(!exitTimeThread) {
				Thread.sleep(200)
				//work with other thread
				runOnUiThread(Runnable {
					if(!threadTimeRunningLoopFunc())exitTimeThread=true
				})
			}
		}).start()
	}
	fun deleteRecord(view : View){
		Genaral.showAskDialog(this,"Warring!","Do you want to delete?",
			fun(dialogInterface: DialogInterface , i:Int){
				try{
					var file = File(recordFilePath)
					if(file.exists())file.delete()
					Genaral.showNotificationDialog(this,"Success!","Record was deleted.",fun(dia:DialogInterface,i:Int){finish()})
//					finish()
				}catch(e:Exception){
					Genaral.showError(this,"Error!","System error, delete record fail.")
				}
			})
	}
	
}