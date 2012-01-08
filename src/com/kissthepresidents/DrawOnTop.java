package com.kissthepresidents;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;
import android.widget.TextView;

public class DrawOnTop extends View {

	private int imageId;
	private Activity activity;
	private Bitmap bitmap;
	private TextView textView;

	public DrawOnTop(Activity activity, int id) {
		super(activity);
		this.activity = activity;
		this.imageId = id;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if(bitmap != null){
			bitmap.recycle();
		}
		bitmap = BitmapFactory.decodeResource(getResources(), imageId);
		Matrix matrix = getMatrix(bitmap);
		canvas.setMatrix(matrix);

		String text = "";
				
		float x = bitmap.getWidth() * 2;
		float y = bitmap.getHeight() /  4;
		switch (imageId) {
		case R.drawable.obama_cut:
			text = "USA";
			break;
		case R.drawable.chavez_cut:
			text = "Venezuela";
			break;
		case R.drawable.hu_jintao_cut:
			text = "China";
			x = 0;
			break;
		case R.drawable.sarkozy_cut:
			text = "France";
			break;
		case R.drawable.north_korea:
			x = 0;
			text = "North Korea";
		default:
			break;
		}

		if(textView != null){
			textView.setText(text);
		}
		
		canvas.drawBitmap(bitmap, x, y, new Paint());
		super.onDraw(canvas);
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
	}
	private Matrix getMatrix(Bitmap bitmap) {
		int windowWidth = activity.getWindow().getDecorView().getWidth();
		int windowHeight = activity.getWindow().getDecorView().getHeight();
		
		Matrix matrix = new Matrix();

		float iWidth = windowWidth / 3;
		float iHeight = windowHeight / 1.2F;

		float bHeight = bitmap.getHeight();
		float bWidth = bitmap.getWidth();

		float wScale = iWidth / bWidth;
		float hScale = iHeight / bHeight;
		matrix.postScale(wScale, hScale);
		return matrix;
	}

	public void releaseImage() {
		bitmap.recycle();
	}

}