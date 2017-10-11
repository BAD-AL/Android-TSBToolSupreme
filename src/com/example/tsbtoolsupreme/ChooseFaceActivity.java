package com.example.tsbtoolsupreme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class ChooseFaceActivity extends Activity 
{
	private ImageView[] mImageViews  = new ImageView[6*28];
	
	OnClickListener imageClickListener = new OnClickListener() {
	    public void onClick(View v) {
	    	onImageClicked( (ImageView) v);
	    }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.face_chooser);
		//mImageView_d4 00-52, 80-d4
		String viewString;
		int id;
		int viewIndex = 0;
		for(int i = 0; i < 0x53; i++)
		{
			viewString = String.format("mImageView_%02x", i); //"mImageView_d4"; 
			id =  getResources().getIdentifier(viewString, "id", this.getPackageName());
			mImageViews[viewIndex] = (ImageView)findViewById(id);
			mImageViews[viewIndex].setTag(viewString);
			mImageViews[viewIndex].setOnClickListener(imageClickListener);
			viewIndex++;
		}
		for(int i = 0x80; i < 0xd5; i++)
		{
			viewString = String.format("mImageView_%02x", i); //"mImageView_d4";
			id =  getResources().getIdentifier(viewString, "id", this.getPackageName());
			mImageViews[viewIndex] = (ImageView)findViewById(id);
			mImageViews[viewIndex].setTag(viewString);
			mImageViews[viewIndex].setOnClickListener(imageClickListener);
			viewIndex++;
		}
	}
	
	private void onImageClicked(ImageView v) 
	{
		Intent resultData = new Intent();
		Object tag =  v.getTag();
		if( tag != null )
		{
			String extraString = tag.toString().replace("mImageView_", "face_");
			resultData.putExtra("imageName", extraString);
			setResult(Activity.RESULT_OK, resultData);
		}
		else
		{
			setResult(Activity.RESULT_CANCELED, resultData);
		}
		
		finish();
	}
	
}
