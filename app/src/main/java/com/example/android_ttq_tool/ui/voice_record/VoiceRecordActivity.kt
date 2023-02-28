package com.example.android_ttq_tool.ui.voice_record

import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.android_ttq_tool.Genaral
import com.example.android_ttq_tool.R
import java.io.File


const val NAVBAR_ITEM_CHANGE_ALPHA_SHOW:Float = 1.0f
const val NAVBAR_ITEM_CHANGE_ALPHA_HIDE:Float = 0.3f
const val NAVBAR_ITEM_CHANGE_MARGIN_BOTTOM_SHOW:Int = 10
const val NAVBAR_ITEM_CHANGE_MARGIN_BOTTOM_HIDE:Int = 0
const val NAVBAR_ITEM_CHANGE_DURATION:Int = 300
const val LAYOUT_CHANGE_ALPHA_SHOW:Float = 1.0f
const val LAYOUT_CHANGE_ALPHA_HIDE:Float = 0f
const val LAYOUT_CHANGE_SCALE_SHOW:Float = 1f
const val LAYOUT_CHANGE_SCALE_HIDE:Float = 0.1f
const val SAVE_FILE_NAME_DEFAULT:String = "VoiceRecording"

class VoiceRecordActivity : AppCompatActivity() {
	
	
	private var layoutNow:String = ""
	//navbar
	private  var newVoiceRecordNumber:Int = 0
	
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
	
	private var pathSaveFileVoiceRecord:String? = null
	private var pathDirectoryVoiceRecord:String? = null
	data class VoiceRecordListItem (var label:String,var checked:Boolean)
	private var voiceRecordList:ArrayList<VoiceRecordListItem>? = null
	
	
	
	var REQUEST_AUDIO_PERMISSION_CODE = 1
	private val REQUEST_READ_FILE_PERMISSION_CODE : Int = 1
	
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
		
		setBackgroundNavbar()
		
		createScreenStart = findViewById(R.id.voiceRecordCreateScreenStart)
		createScreenPause = findViewById(R.id.voiceRecordCreateScreenPause)
		createScreenContinue = findViewById(R.id.voiceRecordCreateScreenContinue)
		createScreenStop = findViewById(R.id.voiceRecordCreateScreenStop)
		createScreenTime = findViewById(R.id.voiceRecordCreateScreenTime)
		
		pathDirectoryVoiceRecord = Environment.getExternalStorageDirectory().absolutePath + "/ttq-tool/VoiceRecord/"
		
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
			//load data
			if(voiceRecordList===null){
				loadVoiceRecord()
			}
			
			layoutNow = "listLayout"
		}
	}
	
	private fun showNewVoiceRecordNumber(){
		findViewById<TextView>(R.id.voiceRecordNavbarNewRecordNumber).text = if(newVoiceRecordNumber==0)"" else "+$newVoiceRecordNumber"
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
	
	private fun setBackgroundNavbar(){
		findViewById<ViewGroup>(R.id.voiceRecordNavBarContainer).setBackgroundColor(Genaral.createBackgroundOpacity(getColor(R.color.background3),0.5f))
	}
	
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
				var mFileName =	pathDirectoryVoiceRecord
				var directory: File = File(mFileName)
				if (! directory.exists()){
					directory.mkdirs();
				}
				mFileName += Genaral.getTimeStringNow()+"."+SAVE_FILE_NAME_DEFAULT+".3gp"
				pathSaveFileVoiceRecord = mFileName
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
	
	private fun saveFileVoiceRecord(input:String?=null):Boolean{
		if(input===null)return true
		var path:String? = pathSaveFileVoiceRecord
		var newName:String = input
		
		var oldFilePath = File(path)
		
		if(oldFilePath.exists()===true){
			path = path!!.replace(SAVE_FILE_NAME_DEFAULT,newName)
			var newFilePath = File(path)
			if(newFilePath.exists()===true){
				Genaral.showError(this,"Error!","File' name is already exist.")
			}else{
				if(oldFilePath.renameTo(newFilePath)===true){
					return true
				}else{
					Genaral.showError(this,"Error!","System error! Can't save with new name.")
				}
			}
		}else{
			Genaral.showError(this,"Error!","System error! Record file is not exist.")
		}
		return false
	}
	
	private fun stopRecording() {
		try{
			mRecorder?.stop()
			mRecorder?.reset()
//			mRecorder?.release()
			recordTimeStart = 0
			changeCreateScreenLayout("readly")
			//alert finish
			var dialog: Dialog = Genaral.createGetStringDialog(this,"Save with a name","VoiceRecording",::saveFileVoiceRecord)
			dialog.show()
			//reset list record loaded
			voiceRecordList=null
			//increase newVoiceRecordNumber
			newVoiceRecordNumber++
			showNewVoiceRecordNumber()
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
	private fun openVoiceRecordListenActivity(fileName : String){
	
	}
	private fun getVoiceRecordListViewItem(context: Context,parent:ViewGroup,label:String):View{
		var item:View = LayoutInflater.from(context).inflate(R.layout.activity_voice_record_list_item, parent, false);
		try{
			val time = label.substring(0,label.indexOf('.')).replace('_',' ')
			val name = label.substring(label.indexOf('.')+1,label.lastIndexOf('.'))
			
			item.findViewById<TextView>(R.id.textViewTime).text = time
			item.findViewById<TextView>(R.id.textViewFileName).text = name
		}catch (e:Exception){
			item.findViewById<TextView>(R.id.textViewFileName).text = label
		}
		
		
		return item
	}
	
	private fun setVoiceRecordListViewItemEvent(view:View, index:Int, isCheckList:Boolean=false){
		if(isCheckList){
			view.setOnClickListener{
				toggleVoiceRecordListViewItemStatus(index,it)
			}
			view.setOnLongClickListener(null)
		}else{
			view.setOnClickListener{
				openVoiceRecordListenActivity(voiceRecordList!!.get(index).label)
			}
			view.setOnLongClickListener{
				changeVoiceRecordListStatus(true, index)
				Genaral.vibrate(this)
				return@setOnLongClickListener true
			}
		}
	}
	
	fun CheckPermissionsReadFile() : Boolean {
		// this method is used to check permission
		val result = ContextCompat.checkSelfPermission(
			applicationContext ,
			WRITE_EXTERNAL_STORAGE
		)
		return result == PackageManager.PERMISSION_GRANTED
	}
	
	
	private fun RequestPermissionsReadFile() {
		showMessageOKCancel(
			"We need to allow access to Read Storage"
		) { dialog:DialogInterface  , which:Int->
			ActivityCompat.requestPermissions(
				this,
				arrayOf<String>(
					WRITE_EXTERNAL_STORAGE
				) ,
				REQUEST_READ_FILE_PERMISSION_CODE
			)
		}
	}
	
	private fun loadVoiceRecord(){
		if (CheckPermissionsReadFile()) {
			try{
				var parent:ViewGroup = findViewById(R.id.voiceRecordListScreenListItem)
				parent.removeAllViews()
				val path:String = pathDirectoryVoiceRecord!!
				voiceRecordList = ArrayList<VoiceRecordListItem>()
				File(path).walkBottomUp().forEach{
//					println(it.toString())
					if(it.isFile)voiceRecordList!!.add(VoiceRecordListItem(it.name,false))
				}
				voiceRecordList!!.sortByDescending { it.label }
				
				for(i in 0..voiceRecordList!!.count()-1){
					var itemList = voiceRecordList!!.get(i)
					var itemView = getVoiceRecordListViewItem(this,parent,itemList.label)
					setVoiceRecordListViewItemEvent(itemView,i,false)
					parent.addView(itemView)
				}
				showHideVoiceRecordListMenuBar(false)
				newVoiceRecordNumber = 0
				showNewVoiceRecordNumber()
			}catch (e:Exception){
				Genaral.showError(this,"Error!","System error, can't load list of voice record files.")
			}
		} else {
			RequestPermissionsReadFile()
		}
	}
	private fun toggleVoiceRecordListViewItemStatus(index:Int, view:View){
		var item = voiceRecordList!!.get(index)
		voiceRecordList!!.set(index,VoiceRecordListItem(item.label,!item.checked))
		changeVoiceRecordListItemIcon(view,if(!item.checked)"check" else "uncheck")
		setVoiceRecordListMenuBarCheckboxLabel()
	}
//	@SuppressLint("CutPasteId")
	private fun setVoiceRecordListMenuBarCheckboxLabel(){
		val progress = voiceRecordList!!.count { it.checked }
		val count = voiceRecordList!!.count()
		var label:String = "All($progress/$count)"
		findViewById<CheckBox>(R.id.voiceRecordListScreenMenuBarCheckbox).text = label
		findViewById<CheckBox>(R.id.voiceRecordListScreenMenuBarCheckbox).isChecked = progress===count
	}
	private fun changeVoiceRecordListStatus(isCheckable:Boolean, id:Int=-1){
		if(isCheckable){
			showHideVoiceRecordListMenuBar(true)
			//reset check
			voiceRecordList!!.replaceAll { it-> VoiceRecordListItem(it.label,false)}
			voiceRecordList!!.set(id, VoiceRecordListItem(voiceRecordList!!.get(id).label, true))
			
			setAllVoiceRecordListItem(true)
			setVoiceRecordListMenuBarCheckboxLabel()
		}else{
			showHideVoiceRecordListMenuBar(false)
			setAllVoiceRecordListItem(false)
		}
	}
	private fun setAllVoiceRecordListItem(isCheckable : Boolean){
		var parent = findViewById<ViewGroup>(R.id.voiceRecordListScreenListItem)
		
		if(isCheckable){
			for(i in 0..parent.childCount-1){
				var view = parent.getChildAt(i)
				changeVoiceRecordListItemIcon(view,if(voiceRecordList!!.get(i).checked)"check" else "uncheck")
				setVoiceRecordListViewItemEvent(view,i,true)
			}
		}else{
			for(i in 0..parent.childCount-1){
				var view = parent.getChildAt(i)
				changeVoiceRecordListItemIcon(view,"none")
				setVoiceRecordListViewItemEvent(view,i,false)
			}
		}
	}
	private fun changeVoiceRecordListItemIcon(item:View,status:String){
		if(status==="none"){
			item.findViewById<ImageView>(R.id.imageViewIcon).setImageResource(R.drawable.voice_recorder)
		}else if(status==="check"){
			item.findViewById<ImageView>(R.id.imageViewIcon).setImageResource(R.drawable.tick_icon)
		}else if(status==="uncheck"){
			item.findViewById<ImageView>(R.id.imageViewIcon).setImageResource(R.drawable.record_icon)
		}
	}
	private fun showHideVoiceRecordListMenuBar(isShow:Boolean=true){
		var menu:ViewGroup = findViewById(R.id.voiceRecordListScreenMenuBar)
		if(isShow===false){
			menu.visibility = ViewGroup.GONE
		}else{
			menu.visibility = ViewGroup.VISIBLE
		}
	}
	fun closeCheckableStatusOfList(view:View){
		showHideVoiceRecordListMenuBar(false)
		setAllVoiceRecordListItem(false)
	}
	fun toggleCheckAll(view:View){
		var checkbox = findViewById<CheckBox>(R.id.voiceRecordListScreenMenuBarCheckbox)
		voiceRecordList!!.replaceAll { it->VoiceRecordListItem(it.label,checkbox.isChecked) }
		setAllVoiceRecordListItem(true)
		setVoiceRecordListMenuBarCheckboxLabel()
	}
	
	fun deleteVoiceRecord(view:View){
		if(voiceRecordList==null||voiceRecordList!!.count { it.checked }==0){
			Genaral.showNotificationDialog(this,"Advise","You should choose one or more record files.")
		}else{
			Genaral.showAskDialog(this,"Warring!","Do you want to delete?",
				fun(dialogInterface:DialogInterface, i:Int){
				val path = pathDirectoryVoiceRecord
				val listDelete = voiceRecordList!!.filter{it.checked}.map {it.label}
				listDelete.forEach {
					var file = File(path+it)
					if(file.exists())file.delete()
				}
				voiceRecordList = null
				loadVoiceRecord()
			 })
			
		}
	}
}