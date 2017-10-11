package com.example.tsbtoolsupreme;

import com.example.tsbtoolsupreme.R.id;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

public class PlaySelectActivity extends Activity {

	private ImageButton[] buttons = new ImageButton[8];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_select_layout);
		
		buttons[0] = (ImageButton)findViewById(R.id.mPlayButton0);
		buttons[1] = (ImageButton)findViewById(R.id.mPlayButton1);
		buttons[2] = (ImageButton)findViewById(R.id.mPlayButton2);
		buttons[3] = (ImageButton)findViewById(R.id.mPlayButton3);
		buttons[4] = (ImageButton)findViewById(R.id.mPlayButton4);
		buttons[5] = (ImageButton)findViewById(R.id.mPlayButton5);
		buttons[6] = (ImageButton)findViewById(R.id.mPlayButton6);
		buttons[7] = (ImageButton)findViewById(R.id.mPlayButton7);
		
		int slot = getIntent().getExtras().getInt("slot");
		String run_pass = "r";
		Object r_p = getIntent().getExtras().get(EditTeamActivity.PASS_RUN);
		if( r_p != null)
			run_pass = r_p.toString();

		String base = run_pass+slot+"_";
		int id = 0;
		for(int i =0; i < buttons.length; i++)
		{
			buttons[i].setOnClickListener(buttonListener);
			buttons[i].setTag(base+i);
			id = getResources().getIdentifier(buttons[i].getTag().toString(), "drawable",
					this.getPackageName());
			buttons[i].setImageResource(id);
		}
	}
	
	private void onButtonClicked(ImageButton v) 
	{
		Intent resultData = new Intent();
		Object tag =  v.getTag();
		if( tag != null )
		{
			String extraString = tag.toString();
			resultData.putExtra("imageName", extraString);
			setResult(Activity.RESULT_OK, resultData);
		}
		else
		{
			setResult(Activity.RESULT_CANCELED, resultData);
		}
		
		finish();
	}
	
	//*********************** Listeners ******************************
	// used for the play buttons 
	View.OnClickListener buttonListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) 
		{
			onButtonClicked((ImageButton)v);
		}
	};
}
