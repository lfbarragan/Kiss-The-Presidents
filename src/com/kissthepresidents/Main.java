package com.kissthepresidents;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class Main extends Activity {

	private Preview preview;
	private Button rightButton;
	private Button leftButton;
	private Camera mCamera;
	private TextView textView;
	private DrawOnTop northKorea;
	private DrawOnTop sarkozy;
	private DrawOnTop juntao;
	private DrawOnTop chavez;
	private DrawOnTop obama;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Hide the window title.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		if (checkCameraHardware()) {
			start();
		} else {
			Toast.makeText(this,
					"Sorry! Can't detect a camera in this device!",
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	private void start() {
		preview = new Preview(this);
		setContentView(preview);

		View main = LayoutInflater.from(getBaseContext()).inflate(
				R.layout.main, null);
		createViews(main);

		addContentView(main, new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.FILL_PARENT));

		showButtons();
	}

	private void showButtons() {
		rightButton.setVisibility(View.VISIBLE);
		leftButton.setVisibility(View.VISIBLE);
		new CountDownTimer(2000, 1) {

			@Override
			public void onFinish() {
				rightButton.setVisibility(View.INVISIBLE);
				leftButton.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onTick(long millisUntilFinished) {
			}

		}.start();
	}

	private void hideButtons() {
		rightButton.setVisibility(View.INVISIBLE);
		leftButton.setVisibility(View.INVISIBLE);
	}

	private void createViews(View parent) {
		// Rest of the view
		final ViewFlipper flipper = (ViewFlipper) parent
				.findViewById(R.id.main_flipper);

		Button cameraButton = (Button) parent.findViewById(R.id.buttons_camera);
		cameraButton.setOnClickListener(new CameraClickListener());
		leftButton = (Button) parent.findViewById(R.id.buttons_left);
		leftButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				flipper.showPrevious();
			}
		});
		rightButton = (Button) parent.findViewById(R.id.buttons_right);
		rightButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				flipper.showNext();
			}
		});

		this.textView = (TextView) parent.findViewById(R.id.text);
		obama = new DrawOnTop(this, R.drawable.obama_cut);
		chavez = new DrawOnTop(this, R.drawable.chavez_cut);
		juntao = new DrawOnTop(this, R.drawable.hu_jintao_cut);
		sarkozy = new DrawOnTop(this, R.drawable.sarkozy_cut);
		northKorea = new DrawOnTop(this, R.drawable.north_korea);

		flipper.addView(obama);
		flipper.addView(chavez);
		flipper.addView(juntao);
		flipper.addView(sarkozy);
		flipper.addView(northKorea);

		flipper.setOnTouchListener(new FlipperOnTouchListener());
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mCamera != null) {
			preview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mCamera = Camera.open();
		preview.setCamera(mCamera);

	}

	private boolean checkCameraHardware() {
		return getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA);
	}

	private class FlipperOnTouchListener implements OnTouchListener {

		private float initX;
		private float currentX;
		private float deltaX;
		private int status;

		private final static int START_DRAGGING = 0;
		private final static int STOP_DRAGGING = 1;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			ViewFlipper flipper = (ViewFlipper) v;
			View currentView = flipper.getCurrentView();
			currentView.setDrawingCacheEnabled(true);

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				initX = event.getX();
				status = START_DRAGGING;
				showButtons();
				break;
			case MotionEvent.ACTION_MOVE:
				if (status == START_DRAGGING) {
					currentX = event.getX();
					deltaX = initX - currentX;

					currentView.setPadding((int) event.getRawX(), 0, 0, 0);

				}
				break;
			case MotionEvent.ACTION_UP:
				currentView.setPadding(0, 0, 0, 0);
				((DrawOnTop) currentView).releaseImage();
				if (deltaX > 50) {
					flipper.showNext();
				} else if (deltaX < -50) {
					flipper.showPrevious();
				}
				updateText(flipper);
				deltaX = 0;
				status = STOP_DRAGGING;
			default:
				break;
			}
			return true;
		}

		private void updateText(ViewFlipper flipper) {
			View v = flipper.getCurrentView();
			if (obama == v) {
				textView.setText("USA");
			} else if (chavez == v) {
				textView.setText("Venezuela");
			} else if (juntao == v) {
				textView.setText("China");
			} else if (sarkozy == v) {
				textView.setText("France");
			} else if (northKorea == v) {
				textView.setText("North Korea");
			}

		}
	}

	private class CameraClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			hideButtons();
			CapturePictureCallback picCallback = new CapturePictureCallback();

			mCamera.takePicture(null, null, picCallback);
		}
	}

	private class CapturePictureCallback implements PictureCallback {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			Bitmap picBitmap = BitmapFactory.decodeByteArray(data, 0,
					data.length);

			final ViewFlipper flipper = (ViewFlipper) findViewById(R.id.main_flipper);
			View flipperView = flipper.getCurrentView();

			Bitmap flipperBitmap = Bitmap.createBitmap(flipperView.getWidth(), flipperView.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas flipperCanvas = new Canvas(flipperBitmap);
			flipperView.draw(flipperCanvas);
			Bitmap resizedFlipperBitmap = Bitmap.createScaledBitmap(flipperBitmap, picBitmap.getWidth(), picBitmap.getHeight(), false);

			// Insert image on top
			Bitmap overlaidBitmap = overlay(picBitmap, resizedFlipperBitmap);
			
			picBitmap.recycle();
			resizedFlipperBitmap.recycle();
			
			// Create file
			save(overlaidBitmap);
			overlaidBitmap.recycle();
			camera.startPreview();
		}
		
	    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
	        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
	        Canvas canvas = new Canvas(bmOverlay);
	        canvas.drawBitmap(bmp1, new Matrix(), null);
	        canvas.drawBitmap(bmp2, new Matrix(), null);
	        return bmOverlay;
	    }


		private void save(Bitmap bitmap) {
			File picturesFolder = null;
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR_MR1) {
				picturesFolder = new File(
						Environment.getExternalStorageDirectory(), "Pictures");
			} else {
				picturesFolder = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
			}

			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
			String dateString = sdf.format(date);
			String fileName = "kissthepresident" + dateString + ".jpeg";
			File pictureFile = new File(picturesFolder, fileName);

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
				bitmap.recycle();

				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR_MR1) {
					ContentValues values = new ContentValues(7);

					values.put(Images.Media.TITLE, fileName);
					values.put(Images.Media.DISPLAY_NAME, fileName);
					values.put(Images.Media.DATE_TAKEN, dateString);
					values.put(Images.Media.MIME_TYPE, "image/jpeg");
					values.put(Images.Media.ORIENTATION, 0);
					values.put(Images.Media.DATA,
							pictureFile.toString());
					values.put(Images.Media.SIZE, pictureFile.length());

					Uri uri = Uri.fromFile(pictureFile);
					getContentResolver().insert(uri, values);

				} else {
		            MediaScannerConnection.scanFile(getApplicationContext(),
		                    new String[] { pictureFile.toString() }, null,
		                    new MediaScannerConnection.OnScanCompletedListener() {
		                public void onScanCompleted(String path, Uri uri) {
		                    Log.i("ExternalStorage", "Scanned " + path + ":");
		                    Log.i("ExternalStorage", "-> uri=" + uri);
		                }
		            });

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}