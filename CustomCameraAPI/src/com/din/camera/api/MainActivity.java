package com.din.camera.api;


import com.din.camera.utils.CameraConstants;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	Button mTakePhotoButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mTakePhotoButton=(Button) findViewById(R.id.take_photo);
        
        mTakePhotoButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MainActivity.this, TakePictureActivity.class);
				intent.putExtra(CameraConstants.CATEGORY_TYPE, 1);
				startActivity(intent);
				MainActivity.this.finish();
			}
		});
    }
    
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }*/
}
