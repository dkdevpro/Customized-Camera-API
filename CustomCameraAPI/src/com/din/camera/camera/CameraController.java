package com.din.camera.camera;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.din.camera.api.PreviewActivity;
import com.din.camera.api.TakePictureActivity;
import com.din.camera.utils.CameraConstants;
import com.din.camera.utils.CameraUtils;
import com.din.camera.utils.CameraWrapper;
import com.din.camera.utils.EGroupType;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.RelativeLayout;


public class CameraController {

	public static final float PHOTO_FRAME_DIMENSION = 5f / 6f;


	
	private Context context;
	
	private Camera camera;
	
	private Point screenResolution;
	
	private Point cameraResolution;
	
	private Point bestPictureSize;

	
	private int currentZoomValue = 0;

	private int zoomModifier = 1;

	private CameraZoomChangedListener zoomChangeListener;

	
	private Rect frameCoordinatesOnScreen;

	
	private Rect frameCoordinatesOnPreview;

	public static boolean isTakingPhoto = false;

	public static boolean autofocusSuccess = false;

	
	private int taskId;

	

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public void setZoomModifier(int mod) {
		zoomModifier = 1;// mod;
	}

	

	private PreviewCallback cameraPreviewCallback = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			Intent intent = new Intent((TakePictureActivity) context,PreviewActivity.class);
			Bundle bundle = new Bundle();
			byte[] jdata =null;
			boolean success = false;
			// we process preview frame only if auto focus success and photo
			// button was pressed
			if (isTakingPhoto && autofocusSuccess) {
				isTakingPhoto = false;
				autofocusSuccess = false;
				switch (camera.getParameters().getPreviewFormat()) {
				case PixelFormat.YCbCr_420_SP:
				case PixelFormat.YCbCr_422_SP:
					try {
						
						Size previewSize = camera.getParameters().getPreviewSize();
						
						YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
						jdata = baos.toByteArray();

						// Convert to Bitmap
						//Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
						byte[] pictureData = createPicture(jdata);
						bundle.putByteArray("pictureData", pictureData);
						
						Log.v("CAMERA", "pictureData: " + pictureData.length);
						
						bundle.putString("pictureName", "jakiś napis");
						
						Log.v("CAMERA", "saving bundle");
						/*byte[] thumbnailData = createThumbnail(source);
						bundle.putSerializable("thumbnailData", new ThumbObject(thumbnailData, 0, 0));*/
						intent.putExtra("data", bundle);
						success = true;
					} catch (Exception e) {
						Log.e("onPictureTaken()", "Error converting bitmap");
						success = false;
					}
					// setting result of activity and finishing
					if (success) {
//						((TakePhoto) context).setResult(Activity.RESULT_OK, intent);
						Log.v("CAMERA", "result ok finish");
						
						((TakePictureActivity) context).startActivity(intent);
						//((TakePictureActivity) context).finish();
						
				//		TakePictureActivity.cameraImage.setImageBitmap(BitmapFactory.decodeByteArray(jdata, 0, jdata.length));
						
				//		TakePictureActivity.shootButton.setVisibility(RelativeLayout.GONE);
				//		TakePictureActivity.retakeButton.setVisibility(RelativeLayout.VISIBLE);
				//		TakePictureActivity.backButton.setVisibility(RelativeLayout.VISIBLE);
//						
						//((TakePictureActivity) context).finish();
						//((TakePictureActivity) context).finishFromCamera(intent);
						
					} else {
						Log.d("CAMERA", "Photo Damaged!!!!");
					}
					break;
				default:
					break;
				}
			}
		}
	};

	
	/*private byte[] createThumbnail(PlanarYUVLuminanceSource source) {
		int[] rgbValues = new int[source.getDataWidth() * source.getDataHeight()];
		source.decodeYUV420SP(rgbValues);
		Bitmap thumbNail = Bitmap.createBitmap(rgbValues, source.getDataWidth(), source.getDataHeight(), Config.ARGB_8888);
		thumbNail = CameraUtils.rotateAndScaleBitmap(thumbNail, 90.0f, 200, 150);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		thumbNail.compress(CompressFormat.JPEG, 60, os);
		return os.toByteArray();
	}*/

	
	private byte[] createPicture(byte[] jdata) {
		Log.v("CAMERA", "createPicture");
		Bitmap bitmap=BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Bitmap bm = CameraUtils.rotateAndScaleBitmap(bitmap, 90.0f, bitmap.getWidth(),bitmap.getHeight());
		bm.compress(CompressFormat.JPEG, CameraConstants.PHOTO_QUALITY, os);
		Log.v("CAMERA", "bm.compress height: " + bm.getHeight());
		return os.toByteArray();
	}

	/**
	 * CameraController object to provide singleton pattern
	 */
	private static CameraController cameraController;

	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * initialize camera controller
	 * 
	 * @param ctx
	 *            context of calling activity
	 * @param task
	 *            id of task
	 */
	public static void init(Context ctx, int task) {
		if (cameraController == null) {
			cameraController = new CameraController(ctx, task);
		}
	}

	/**
	 * Constructor for CameraController class
	 * 
	 * @param ctx
	 *            context of calling activity
	 * @param task
	 *            id of task
	 */
	private CameraController(Context ctx, int task) {
		this.context = ctx;
		this.taskId = task;

	}

	public static CameraController getController() {
		return cameraController;
	}

	/**
	 * opens camera and set surface holder for it
	 * 
	 * @param holder
	 *            surface holder for camera
	 * @throws Exception
	 *             if camera.open fail
	 */
	public void cameraOpen(SurfaceHolder holder) throws IOException {
		if (camera == null) {
			camera = Camera.open();
			// if (CameraConstants.ANALYTICS_ON) {
			// tracker = GoogleAnalyticsTracker.getInstance();
			// tracker.startNewSession(CameraConstants.ANALYTICS_API_KEY, 30,
			// context);
			// }
		}
		if (camera == null) {
			// if (CameraConstants.ANALYTICS_ON) {
			// tracker.stopSession();
			// }
			Log.e("Camera error:", "camera.open() returned null");
			throw new IOException();
		}
		camera.setPreviewDisplay(holder);
		initResolutionsFromParams(camera);
		setBestResolutionParams(camera);
		camera.setPreviewCallback(cameraPreviewCallback);
	}

	/**
	 * release android camera object
	 */
	public void releaseCamera() {
		frameCoordinatesOnPreview = null;
		frameCoordinatesOnScreen = null;
		if (camera != null) {
			// if (CameraConstants.ANALYTICS_ON) {
			// tracker.stopSession();
			// }
			camera.release();
			camera = null;
		}
	}

	/**
	 * start camera preview
	 */
	public void startPreview() {
		if (camera != null) {
			camera.startPreview();
		}
	}

	/**
	 * stop camera preview
	 */
	public void stopPreview() {
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
		}
	}

	public void switchFlash(boolean on) {
		Camera.Parameters params = camera.getParameters();
		if (on) {
			params.setFlashMode(Parameters.FLASH_MODE_ON);
		} else {
			params.setFlashMode(Parameters.FLASH_MODE_OFF);
		}
	}

	/**
	 * zoom camera to the given value
	 * 
	 * @param value
	 *            - zoom value
	 */
	public void zoom(int value) {
		if (camera != null) {

			Camera.Parameters params = camera.getParameters();
			if (value < 0)
				value = 0;
			if (value > CameraWrapper.getMaxZoom(params))// params.getMaxZoom())
				value = CameraWrapper.getMaxZoom(params);// params.getMaxZoom();
			if (CameraWrapper.isZoomSupported(params) && currentZoomValue != value) {
				currentZoomValue = value;
				CameraWrapper.setZoom(params, currentZoomValue);
				// params.setZoom(currentZoomValue);
				camera.setParameters(params);
				if (zoomChangeListener != null) {
					zoomChangeListener.zoomChanged(currentZoomValue);
				}
				Log.d("ZOOM", "zoom set to " + currentZoomValue);
			}
		}
	}

	public boolean isZoomSupported() {
		return CameraWrapper.isZoomSupported(camera.getParameters());
	}

	public int getMaxZoom() {
		// return camera.getParameters().getMaxZoom();
		return CameraWrapper.getMaxZoom(camera.getParameters());
	}

	/**
	 * Zoom in by mod
	 */
	public void zoomIn() {
		zoom(currentZoomValue + zoomModifier);
	}

	/**
	 * Zoom out by mod
	 */
	public void zoomOut() {
		zoom(currentZoomValue - zoomModifier);
	}

	public void zoomByRange(int range) {
		zoom(currentZoomValue + range);
	}

	/**
	 * 
	 * @param prc
	 *            - %
	 */
	public void zoomByPrc(float prc) {
		if (camera != null) {
			Camera.Parameters params = camera.getParameters();
			int zoomVal = (int) (CameraWrapper.getMaxZoom(params) * prc / 100);
			Log.d("ZOOM",
					String.format("Prc zoom pr = %f; maxZoom = %d; zoomVal = %d", prc, CameraWrapper.getMaxZoom(params), zoomVal));
			zoom(zoomVal);
		}
	}

	/**
	 * 
	 * @param scale
	 *            - %
	 */
	public void zoomByScale(float scale) {
		if (camera != null) {
			int newZoomValue = (int) (scale * (currentZoomValue + 1));
			zoom(--newZoomValue);
		}
	}

	/**
	 * use camera autofocus feature
	 */
	public void autoFocus() {
		// redrawing frame on preview to indicate that picture is being
		// processed
		((TakePictureActivity) context).redrawFrame(Color.RED, 200);
		camera.autoFocus(mAutoFocusCallback);
	}

	/**
	 * sets camera resolution and best picture size
	 * 
	 * @param camera
	 *            camera object
	 */
	private void initResolutionsFromParams(Camera camera) {
		Camera.Parameters params = camera.getParameters();
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		screenResolution = new Point(display.getWidth(), display.getHeight());
		cameraResolution = findBestPreviewOrPictureSize(params.getSupportedPreviewSizes(), screenResolution, false);
		bestPictureSize = findBestPreviewOrPictureSize(params.getSupportedPictureSizes(), screenResolution, true);
	}

	/**
	 * sets params for camera, default orientation for android is landscape, we
	 * use camera in portrait mode so when we set preview and picture size we
	 * swap x and y values.
	 * 
	 * @param camera
	 *            android camera object
	 */
	private void setBestResolutionParams(Camera camera) {

		Camera.Parameters params = camera.getParameters();
		params.set("jpeg-quality", "100");
		params.setPictureFormat(PixelFormat.JPEG);
		params.setPreviewSize(cameraResolution.x, cameraResolution.y);

		// TODO: remove or add settings?:
		if (CameraConstants.USE_CAMERA_AUTO_FLASH)
			params.setFlashMode(Parameters.FLASH_MODE_AUTO);

		camera.setParameters(params);
		getFrameCoordinates();
		getFramingRectInPreview();
	}

	/**
	 * find best preview size or picture size. Best mean closest to screen
	 * resolution in that case.
	 * 
	 * @param availablePreviewSizes
	 *            list containing all available preview sizes
	 * @param screenResolution
	 *            resolution of phone screen
	 * @return point (x,y) that contains best resolution
	 */
	private Point findBestPreviewOrPictureSize(List<Size> availablePreviewSizes, Point screenResolution, boolean isPicture) {
		int bestX = 0;
		int bestY = 0;
		int diff = Integer.MAX_VALUE;
		if (availablePreviewSizes == null)
			return screenResolution;
		for (Size size : availablePreviewSizes) {
			int newX = 0;
			int newY = 0;
			try {
				newX = size.width;
				newY = size.height;
			} catch (NumberFormatException nfe) {
				Log.e("error finding preview", "number format exception for:" + newX + " " + newY);
				continue;
			}
			// if widest dimension is less than 480 we stop searching
			if (isPicture && Math.max(newX, newY) < 480) {
				break;
			}
			// we looking for size that is closest to screen resolution
			int newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y);
			if (newDiff == 0) {
				bestX = newX;
				bestY = newY;
				break;
			} else if (newDiff < diff) {
				bestX = newX;
				bestY = newY;
				diff = newDiff;
			}
		}
		if (bestX > 0 && bestY > 0) {
			return new Point(bestX, bestY);
		}
		return null;
	}

	/**
	 * code from barcode scanner to find coordinates of rectangular frame drawn
	 * on preview surface
	 * 
	 * @return Rect object containing coordinates
	 */
	public Rect getFrameCoordinates() {
		int width = 0;
		int height = 0;
		// we set proper frame size for different product categories
		switch (taskId) {
		case EGroupType.eGROUP_MOVIE:
		case EGroupType.eGROUP_GAME:
		case EGroupType.eGROUP_BOOK:
			width = (int) (screenResolution.x * PHOTO_FRAME_DIMENSION);
			height = (int) (screenResolution.y * PHOTO_FRAME_DIMENSION);
			break;
		}
		if (frameCoordinatesOnScreen == null) {
			if (camera == null) {
				return null;
			}
			int leftOffset = (screenResolution.x - width) / 2;
			int topOffset = (screenResolution.y - height) / 2;
			frameCoordinatesOnScreen = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
		}
		return frameCoordinatesOnScreen;
	}

	/**
	 * compute frame border on the actual picture, this is the area we want to
	 * crop from the picture
	 * 
	 * @return rect containing frame coordinates on picture
	 */
	public Rect getFramingRectInPreview() {
		if (frameCoordinatesOnPreview == null) {
			int left = frameCoordinatesOnScreen.left * cameraResolution.x / screenResolution.x;
			int right = frameCoordinatesOnScreen.right * cameraResolution.x / screenResolution.x;
			int top = frameCoordinatesOnScreen.top * cameraResolution.y / screenResolution.y;
			int bottom = frameCoordinatesOnScreen.bottom * cameraResolution.y / screenResolution.y;
			frameCoordinatesOnPreview = new Rect(left, top, right, bottom);
		}
		return frameCoordinatesOnPreview;
	}

	public Rect getFramingRectInPicture() {
		if (frameCoordinatesOnPreview == null) {
			int left = frameCoordinatesOnScreen.left * bestPictureSize.x / screenResolution.x;
			int right = frameCoordinatesOnScreen.right * bestPictureSize.x / screenResolution.x;
			int top = frameCoordinatesOnScreen.top * bestPictureSize.y / screenResolution.y;
			int bottom = frameCoordinatesOnScreen.bottom * bestPictureSize.y / screenResolution.y;
			frameCoordinatesOnPreview = new Rect(left, top, right, bottom);
		}
		return frameCoordinatesOnPreview;
	}

	/**
	 * wrapper for function returning luminance source
	 * 
	 * @param data
	 *            preview data
	 * @param frameCoordinates
	 *            coordinates of frame placed on screen
	 * @return lumimnance source
	 */
	/*public PlanarYUVLuminanceSource getLuminanceSource(byte[] data, Rect frameCoordinates) {
		return buildLuminanceSource(data, cameraResolution.x, cameraResolution.y, frameCoordinates);
	}*/

	/**
	 * returns luminance source
	 * 
	 * @param data
	 *            data from preview
	 * @param width
	 *            data width
	 * @param height
	 *            data height
	 * @param frameCoordinates
	 *            coordinates of frame placed on screen
	 * @return luminance source
	 */
	/*private PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height, Rect frameCoordinates) {
		PlanarYUVLuminanceSource source = null;
		if (frameCoordinates == null) {
			frameCoordinates = new Rect(0, 0, width, height);
		}
		switch (camera.getParameters().getPreviewFormat()) {
		case PixelFormat.YCbCr_420_SP:
		case PixelFormat.YCbCr_422_SP:
			source = new PlanarYUVLuminanceSource(data, width, height, frameCoordinates.left, frameCoordinates.top,
					frameCoordinates.width(), frameCoordinates.height());
			break;
		default:
			break;
		}
		return source;
	}*/

	/**
	 * callback object, its called when autofocus is complete
	 */
	private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback() {

		/**
		 * function called when auto focus is completed
		 * 
		 * @param success
		 *            tells if autofocus where successfully acomplished
		 * @param camera
		 *            android camera object
		 */
		public void onAutoFocus(boolean success, Camera camera) {
			autofocusSuccess = true;
		}
	};

	public Camera getCamera() {
		return camera;
	}

	public Context getContext() {
		return context;
	}

	public Point getScreenResolution() {
		return screenResolution;
	}

	public Point getCameraResolution() {
		return cameraResolution;
	}

	public void setCallback() {
		if (camera != null) {
			camera.setPreviewCallback(cameraPreviewCallback);
		}
	}

	public CameraZoomChangedListener getZoomChangeListener() {
		return zoomChangeListener;
	}

	public void setZoomChangeListener(CameraZoomChangedListener zoomChangeListener) {
		this.zoomChangeListener = zoomChangeListener;
	}

}
