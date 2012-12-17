package com.din.camera.api;

import java.io.IOException;

import com.din.camera.camera.CameraController;
import com.din.camera.camera.CameraZoomChangedListener;
import com.din.camera.camera.PhotoFrameView;
import com.din.camera.utils.CameraConstants;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;


public class TakePictureActivity extends Activity {

	private final String TAG = "CAMERA";

	public static final String activityId = "TakePhoto";

	private CameraController cameraController;
	
	private SurfaceHolder holder;
	
	private SurfaceView previewView;
	
	private boolean takingPhoto = false;

	private PhotoFrameView frameView;

	private int categoryType;
	
	public static RelativeLayout cameraLayout;
	private  Button shootButton;
	

	private SeekBar zoomer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_preview);
		
		shootButton=(Button) findViewById(R.id.shootPhoto);
		cameraLayout=(RelativeLayout) findViewById(R.id.cameraLayout);
		
		categoryType = getIntent().getIntExtra(CameraConstants.CATEGORY_TYPE, 0);
		frameView = (PhotoFrameView) findViewById(R.id.bookCoverFrame);
		try {
			initCamera(categoryType);
		} catch (IOException e) {
			Log.e("onCreate()", "error in initCamera()");
		}
		
		

	}

	
	private void initCamera(int categoryType) throws IOException {
		CameraController.init(TakePictureActivity.this, categoryType);
		cameraController = CameraController.getController();
		cameraController.setTaskId(categoryType);
		cameraLayout.setOnKeyListener(mKeyListener);
		shootButton.setOnClickListener(mShootPhotoListener);

		previewView = (SurfaceView) findViewById(R.id.preview);
		holder = previewView.getHolder();
		holder.addCallback(holderCallback);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		int orientation=getWindowManager().getDefaultDisplay().getOrientation();
		int rotation=getWindowManager().getDefaultDisplay().getRotation();
		Log.v(TAG, "Orientation = "+orientation +" rotation = "+rotation);
		
		takingPhoto = false;
		cameraController.setContext(this);
		cameraController.startPreview();
		
	}
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Toast.makeText(this, "TakePicture-onConfigurationChanged", Toast.LENGTH_LONG).show();
	}


	@Override
	protected void onPause() {
		super.onPause();
		cameraController.stopPreview();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}

	private SurfaceHolder.Callback holderCallback = new SurfaceHolder.Callback() {

		public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
			cameraController.stopPreview();
			cameraController.releaseCamera();
		}

		public void surfaceCreated(SurfaceHolder surfaceHolder) {
			try {
				CameraController.getController().cameraOpen(surfaceHolder);

				int maxZoom = cameraController.getMaxZoom();
				zoomer = (SeekBar) findViewById(R.id.zoomSlider);
				if (cameraController.isZoomSupported() && maxZoom > 1) {
					zoomer.setMax(maxZoom);
					zoomer.setOnSeekBarChangeListener(sbListener);
				} else {
					zoomer.setVisibility(View.INVISIBLE);
				}

				cameraController.setZoomChangeListener(pinchZoomListener);

				CameraController.getController().startPreview();
			} catch (Exception e) {

				Log.e("Error in surfaceCreated:", "camera controller problem");

			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			cameraController.startPreview();
		}
	};

	
	private View.OnKeyListener mKeyListener = new View.OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_CAMERA) {
				if (event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN) {
					CameraController.getController().autoFocus();
				}
				return true;
			}
			return false;
		}
	};

	
	private OnSeekBarChangeListener sbListener = new OnSeekBarChangeListener() {

		public void onStopTrackingTouch(SeekBar seekBar) {

		}

		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				CameraController.getController().zoom(progress);
			}
		}
	};

	
	private CameraZoomChangedListener pinchZoomListener = new CameraZoomChangedListener() {

		public void zoomChanged(int value) {
			zoomer.setProgress(value);
		}

	};

	private OnClickListener mShootPhotoListener = new OnClickListener() {
		public void onClick(View v) {
			if (!takingPhoto) {
				
				((Button) v).setEnabled(false);
				takingPhoto = true;
				CameraController.isTakingPhoto = true;
				cameraController.autoFocus();
			}
		}
	};

	public void redrawFrame(int color, long delayInMilis) {
		frameView.redraw(color, delayInMilis);
	}

	public void finishFromCamera(Intent intent) {
		if (intent != null) {
			Log.v(TAG, "data != null");
			Bundle bundle = intent.getBundleExtra("data");
			if (bundle != null) {
				Log.v(TAG, "bundle != null");

				String napis = "AA";
				napis = bundle.getString("pictureName");
				// Log.v(TAG, napis);

				byte[] pictureData = bundle.getByteArray("pictureData");
				Bitmap image = null;
				if (pictureData != null) {
					Log.v(TAG, "pictureData != null");
					try {
						image = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
					
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (image != null) {
						Log.v(TAG, "image != null");

					}
				} else {
					Log.v(TAG, "picture data == null");
				}
				// ThumbObject thumb = bundle.getSerializable("thumbnailData");
			}
		}

		setResult(Activity.RESULT_OK, intent);
		Log.v("CAMERA", "result take photo ok finish");
		finish();

	}
	
}
