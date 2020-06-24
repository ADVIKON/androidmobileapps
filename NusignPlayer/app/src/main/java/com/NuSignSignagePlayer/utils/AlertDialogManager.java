package com.NuSignSignagePlayer.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;


public class AlertDialogManager {
	 /**
	  * Function to display simple Alert Dialog
	  * @param context - application context
	  * @param title - alert dialog title
	  * @param message - alert message
	  * @param status - success/failure (used to set icon)
	  *      - pass null if you don't want icon
	  * */
	 @SuppressWarnings("deprecation")
	 public void showAlertDialog(final Activity context, String title, String message,
								 final Boolean status) {
	  AlertDialog alertDialog = new AlertDialog.Builder(context).create();

	  // Setting Dialog Title
	  alertDialog.setTitle(title);

	  // Setting Dialog Message
	  alertDialog.setMessage(message);

	  if(status != null)
	  // Setting OK Button
	  alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
	   public void onClick(DialogInterface dialog, int which) {
		   if(status == true)
		   {
			context.finish();
		   }
//		   context.finish();
//		   NavUtils.navigateUpTo(context,context.getIntent());
		   
	   }
	  });

	  // Showing Alert Message
	  alertDialog.show();
	 }
	}