package com.din.camera.api;

import com.din.camera.utils.CameraConstants;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class PreviewActivity extends Activity {
	
	public static Button backButton,retakeButton;
	
	public static ImageView cameraImage;
	
	Button mTakePhotoButton;
	private String TAG="-PreviewActivity-";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
      
        Bundle bundle=getIntent().getBundleExtra("data");
       
        
		retakeButton=(Button) findViewById(R.id.recapture_button);
		 retakeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(PreviewActivity.this, TakePictureActivity.class);
				startActivity(intent);
			}
		});
		
		backButton=(Button) findViewById(R.id.back);
		backButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(PreviewActivity.this, MainActivity.class);
				startActivity(intent);
			}
		});
		
		cameraImage=(ImageView) findViewById(R.id.cameraImage);
        
        byte[] pictureData = bundle.getByteArray("pictureData");
     
		if (pictureData != null) {
			Log.v(TAG, "pictureData != null");
			
			cameraImage.setImageBitmap(BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length));
		} else {
			
			Log.v(TAG, "picture data == null");
		}
    }
    
   
    
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Toast.makeText(this, "PreviewActivity-onConfigurationChanged", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onConfigurationChanged");
		
	}
    
}
