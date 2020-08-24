package com.advikonsignagekeyboardplayer.utils;


import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.advikonsignagekeyboardplayer.R;
import com.advikonsignagekeyboardplayer.models.Songs;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

public class Utilities {
	
	/**
	 * Function to convert milliseconds time to
	 * Timer Format
	 * Hours:Minutes:Seconds
	 * */
	public static String milliSecondsToTimer(long milliseconds){
		String finalTimerString = "";
		String secondsString = "";
		String minuteString = "";
		
		// Convert total duration into time
		   int hours = (int)( milliseconds / (1000*60*60));
		   int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
		   int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
		   // Add hours if there
		   if(hours > 0){
			   finalTimerString = hours + ":";
		   }
		   if(minutes < 10){ 
			   minuteString = "0"+minutes;
		   }else{
			   minuteString = ""+minutes;
			   }
		   // Prepending 0 to seconds if it is one digit
		   if(seconds < 10){ 
			   secondsString = "0" + seconds;
		   }else{
			   secondsString = "" + seconds;
			   }
		   
		   finalTimerString = finalTimerString + minuteString + ":" + secondsString;
		
		// return timer string
		return finalTimerString;
	}

	public static boolean isVersionLowerThanLollipop (){

		return (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP);
	}

	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @author paulburke
	 */
	public static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] {
						split[1]
				};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {

			// Return the remote address
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
									   String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}


	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}

	/**
	 * Function to get Progress percentage
	 * @param currentDuration
	 * @param totalDuration
	 * */
	public int getProgressPercentage(long currentDuration, long totalDuration){
		Double percentage = (double) 0;

		long currentSeconds = (int) (currentDuration / 1000);
		long totalSeconds = (int) (totalDuration / 1000);

		// calculating percentage
		percentage =(((double)currentSeconds)/totalSeconds)*100;

		// return percentage
		return percentage.intValue();
	}

	/**
	 * Function to change progress to timer
	 * @param progress -
	 * @param totalDuration
	 * returns current duration in milliseconds
	 * */
	public int progressToTimer(int progress, int totalDuration) {
		int currentDuration = 0;
		totalDuration = (int) (totalDuration / 1000);
		currentDuration = (int) ((((double)progress) / 100) * totalDuration);

		// return current duration in milliseconds
		return currentDuration * 1000;
	}

	public static int getDayNumber(String day){
		int dayNumber = 0;
		if (day.equals("Monday")){
			dayNumber = 1;
		}else if (day.equals("Tuesday")){
			dayNumber = 2;
		}else if (day.equals("Wednesday")){
			dayNumber = 3;
		}else if (day.equals("Thursday")){
			dayNumber = 4;
		}else if (day.equals("Friday")){
			dayNumber = 5;
		}else if (day.equals("Saturday")){
			dayNumber = 6;
		}else if (day.equals("Sunday")){
			dayNumber = 7;
		}

		return dayNumber;
	}

//Get day number For Advertidsement

	public static void showToast(final Context context,final String message){

		Handler handler = new Handler(Looper.getMainLooper());

		handler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}
		});


//        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}


	public static int getDayNumberForAdv(){

		SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
		Date d = new Date();
		String day = sdf.format(d);

		int dayNumber = 0;
		if (day.equals("Sunday")){
			dayNumber = 1;
		}else if (day.equals("Monday")){
			dayNumber = 2;
		}else if (day.equals("Tuesday")){
			dayNumber = 3;
		}else if (day.equals("Wednesday")){
			dayNumber = 4;
		}else if (day.equals("Thursday")){
			dayNumber = 5;
		}else if (day.equals("Friday")){
			dayNumber = 6;
		}else if (day.equals("Saturday")){
			dayNumber = 7;
		}

		return dayNumber;
	}


	public static Typeface getApplicationTypeface (Context context){
		return Typeface.createFromAsset(context.getAssets(),context.getString(R.string.century_font));
	}

	public static String getDeviceID(Context context){

//		return "23b8f0b8a6de10fevideo"; // TODO Comment this line.
		return Settings.Secure.getString(context.getContentResolver(),
				Settings.Secure.ANDROID_ID) + Constants.VIDEO_TAG;
	}

	public static boolean isConnected(){
		return ConnectivityReceiver.isConnected();
	}

	public static int 	getCurrentDayNumber(){

		SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
		Date d = new Date();
		String dayOfTheWeek = sdf.format(d);
		return Utilities.getDayNumber(dayOfTheWeek);
	}

	public static String changeDateFormat(String startTime) {

		String formattedDate = null;

		DateFormat readFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa");
		DateFormat writeFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
		Date date = null;
		try {
			date = readFormat.parse(startTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (date != null) {
			formattedDate = writeFormat.format(date);
		}
		return formattedDate;
	}

	public static long getTimeInMilliSec(String startTime1) {
		SimpleDateFormat sdf1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
		long milliSec = 0;
		try {
			Date mDate = sdf1.parse(startTime1);
			milliSec = mDate.getTime();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(mDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return milliSec;
	}

	public void sort(ArrayList<Songs> songsArrayList){

		try {
			Collections.sort(songsArrayList, new Comparator<Songs>() {
				public int compare(Songs p1, Songs p2) {
					return Long.valueOf(p1.getSerialNo()).compareTo(p2.getSerialNo());
				}
			});
		} catch (Exception e){

			Log.e("Sort exception","");
			e.printStackTrace();
		}

	}

	public static String removeSpecialCharacterFromFileName(String fileName){
		return fileName.replace(" ", "_").
				replace("*", "").
				replace("'", "").
				replace("&","").
				replace("-","").
				replace("!","").
				replace("$","").
				replace("#","").
				replace("^","").
				replace("@","")
				;
	}

	public static Uri getImageContentUri(Context context, File imageFile) {
		String filePath = imageFile.getAbsolutePath();
		Cursor cursor = context.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Images.Media._ID },
				MediaStore.Images.Media.DATA + "=? ",
				new String[] { filePath }, null);
		if (cursor != null && cursor.moveToFirst()) {
			int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
			cursor.close();
			return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
		} else {
			if (imageFile.exists()) {
				ContentValues values = new ContentValues();
				values.put(MediaStore.Images.Media.DATA, filePath);
				return context.getContentResolver().insert(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			} else {
				return null;
			}
		}
	}

	public static String currentDate(){
		Calendar calendar;
		calendar =Calendar.getInstance();
		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("dd/MMM/yyyy");
		String played_date = simpleDateFormat1.format(calendar.getTime());
		return played_date;
	}

	public static String currentTime(){
		Calendar calendar;
		calendar =Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss aa");
		String played_Time = simpleDateFormat.format(calendar.getTime());
		return played_Time;
	}

	public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static String currentTimeHHMM(){
		Calendar calendar;
		calendar =Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aa",Locale.US);
		String played_Time = simpleDateFormat.format(calendar.getTime());
		return played_Time;
	}

	public static String currentTimeexit(){
		Calendar calendar;
		calendar =Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aa");
		String played_Time = simpleDateFormat.format(calendar.getTime());
		return played_Time;
	}

	public static String getAndroidVersion() {
		String release = Build.VERSION.RELEASE;
		int sdkVersion = Build.VERSION.SDK_INT;
		return  "" +  release ;
	}

	public static String changeDateFormatForPrayer(String timePrayer) {


		DateFormat readFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa");
		DateFormat writeFormat = new SimpleDateFormat("EEE MMM dd HH:mm z yyyy");
		Date date = null;
		try {
			date = readFormat.parse(timePrayer);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		String formattedDate = null;

		if (date != null) {
			formattedDate = writeFormat.format(date);
		}
		return formattedDate;
	}

	public static long getTimeInMilliSecForPrayer(String timePrayer) {
		SimpleDateFormat sdf1 = new SimpleDateFormat("EEE MMM dd HH:mm z yyyy");
		long milliSec = 0;
		try {
			Date mDate = sdf1.parse(timePrayer);
			milliSec = mDate.getTime();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(mDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return milliSec;
	}

	public static String currentFormattedDate(){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		Date date = new Date();
		return simpleDateFormat.format(date);
	}

	public static String convertToBase64(String fileName){
		try {
			byte[] data = fileName.getBytes("UTF-8");
			return Base64.encodeToString(data, Base64.DEFAULT);
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public static String decodeFromBase64(String encodedFileName){
		try {
			byte[] data = Base64.decode(encodedFileName, Base64.DEFAULT);
			return new String(data, "UTF-8");
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public static String removeFileExtension(String fileName, Context context){
		if (fileName.endsWith(context.getString(R.string.mp4_file_extension)))
			return fileName.substring(0,fileName.length() - context.getString(R.string.mp4_file_extension).length());
		else
			return fileName;
	}

	public static long getDifferenceBetweenTwoDatesInDays(Date startDate, Date endDate) {
		//milliseconds
		long different = endDate.getTime() - startDate.getTime();

		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;

		long elapsedDays = different / daysInMilli;
		different = different % daysInMilli;

		long elapsedHours = different / hoursInMilli;
		different = different % hoursInMilli;

		long elapsedMinutes = different / minutesInMilli;
		different = different % minutesInMilli;

		long elapsedSeconds = different / secondsInMilli;

		if (elapsedDays < 0){
			elapsedDays = -(elapsedDays);
		}

		return elapsedDays;
	}

	public static void saveTankPercentages(Context context, ArrayList<Integer> percentages){
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < percentages.size(); i++) {
			str.append(percentages.get(i)).append(",");
		}
		SharedPreferenceUtil.setStringPreference(context, AlenkaMediaPreferences.tankAlertPercentages,str.toString());
	}

	public static ArrayList<Integer> getTankPercentages(Context context){
		String savedString = SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.tankAlertPercentages);
		String[] components = savedString.split(",");
		StringTokenizer st = new StringTokenizer(savedString, ",");
		ArrayList<Integer> savedList = new ArrayList<>();
		for (int i = 0; i < components.length; i++) {
			savedList.add(Integer.parseInt(components[i]));
		}
		return savedList;
	}

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	public static boolean isEmulator() {
		return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
				|| Build.FINGERPRINT.startsWith("generic")
				|| Build.FINGERPRINT.startsWith("unknown")
				|| Build.HARDWARE.contains("goldfish")
				|| Build.HARDWARE.contains("ranchu")
				|| Build.MODEL.contains("google_sdk")
				|| Build.MODEL.contains("Emulator")
				|| Build.MODEL.contains("Android SDK built for x86")
				|| Build.MANUFACTURER.contains("Genymotion")
				|| Build.PRODUCT.contains("sdk_google")
				|| Build.PRODUCT.contains("google_sdk")
				|| Build.PRODUCT.contains("sdk")
				|| Build.PRODUCT.contains("sdk_x86")
				|| Build.PRODUCT.contains("vbox86p")
				|| Build.PRODUCT.contains("emulator")
				|| Build.PRODUCT.contains("simulator");
	}

	public static String getLztekMacAddress(Context context){
		try {
//			com.lztek.toolkit.Lztek mLztek = com.lztek.toolkit.Lztek.create(context);
						String macAddress = "";
//			String macAddress = mLztek.getEthMac().toUpperCase();

			if (macAddress != null){
				return macAddress;
			}

		}catch (Exception e){
			e.printStackTrace();
		}
		return "";
	}
}

