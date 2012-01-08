package com.kissthepresidents;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class FacebookHandler {

	private String FACEBOOK_APP_ID = "339004799446641";	
	
	private Facebook facebook = new Facebook(FACEBOOK_APP_ID);

	private Activity activity;
	
	
	public FacebookHandler(Activity activity){
		this.activity = activity;
	}
	/*
	 * Starts the Facebook authentication process and sets the handler to
	 * process the users response
	 * 
	 * @returns Access token
	 */
	public void authenticateWithFacebook(Bitmap bitmap) {
		facebook.authorize(activity, new String[] { "publish_stream" },
				new FacebookLoginHandler(bitmap));
	}

	/*
	 * Inner class to handle Facebook dialog events
	 */
	private class FacebookLoginHandler implements DialogListener {

		private Bitmap bitmap;
		private static final String FACEBOOK_ACCESS_TOKEN_PARAM = "access_token";

		public FacebookLoginHandler(Bitmap bitmap) {
			this.bitmap = bitmap;
		}

		/*
		 * On complete check if the access token is available. If so, then
		 * update the user account and synch the data. Web service server side
		 * will use the token/sid to access Facebook, get the user id and make
		 * sure that this user exists If it doesn't then it will create one.
		 */
		public void onComplete(Bundle values) {
			String accessToken = (String) values
					.get(FACEBOOK_ACCESS_TOKEN_PARAM);
			if (accessToken != null) {

				shareOnFacebook(bitmap);

			} else {
				showAlertMessage("An error occurred while authenticating with Facebook");
			}
		}

		@Override
		public void onCancel() {
		}

		@Override
		public void onError(DialogError e) {
		}

		@Override
		public void onFacebookError(FacebookError e) {
		}
	}

	private void shareOnFacebook(Bitmap bi) {
		byte[] data = null;

		// Bitmap bi = BitmapFactory.decodeFile(photoToPost);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bi.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		data = baos.toByteArray();

		Bundle params = new Bundle();
		params.putString("method", "photos.upload");
		params.putByteArray("picture", data);
		params.putString("app_id", FACEBOOK_APP_ID);

		AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
		mAsyncRunner.request(null, params, "POST", new UploadListener(bi));

	}

	private void showAlertMessage(String message) {
		// Toast.makeText(getApplicationContext(), message,
		// Toast.LENGTH_LONG).show();
		/*
		 * AlertDialog alert = new AlertDialog.Builder(this).create();
		 * alert.setMessage(message); alert.setButton("Close", new
		 * DialogInterface.OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface alert, int arg1) {
		 * alert.dismiss(); }
		 * 
		 * });
		 * 
		 * alert.show();
		 */

	}

	private class UploadListener implements AsyncFacebookRunner.RequestListener {

		private Bitmap bitmap;

		public UploadListener(Bitmap bitmap) {
			this.bitmap = bitmap;
		}

		@Override
		public void onComplete(String response) {
			bitmap.recycle();
			showAlertMessage("Upload completed");
		}

		@Override
		public void onIOException(IOException e) {
			showAlertMessage("Upload failed due to an IOException");
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e) {
			showAlertMessage("Upload failed, file not found");
		}

		@Override
		public void onMalformedURLException(MalformedURLException e) {
			showAlertMessage("Upload failed due to malformed url");
		}

		@Override
		public void onFacebookError(FacebookError e) {
			showAlertMessage("Facebook error while uploading image");
		}

	}
}
