package com.example.tsbtoolsupreme;


import java.net.URISyntaxException;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditSelector extends Activity 
{

	private static final int LAUNCH_PLAYER_EDITOR   = 3;
	private static final int LAUNCH_TEAM_EDITOR     = 4;
	private static final int LAUNCH_SCHEDULE_EDITOR = 5;
	private static final int LAUNCH_ADVANCED_EDITOR = 6;
	private static final int LAUNCH_HACK_ACTIVITY   = 7;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_selector_layout);
		
		Button editPlayersButton = (Button)findViewById(R.id.mEditPlayersButton);
		editPlayersButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EditSelector.this, EditPlayerActivity.class); // launch screen2
				startActivityForResult(intent, LAUNCH_PLAYER_EDITOR);
			}
		});
		
		Button editTeamButton = (Button)findViewById(R.id.mEditTeamsButton);
		editTeamButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EditSelector.this, EditTeamActivity.class); // launch screen2
				startActivityForResult(intent, LAUNCH_TEAM_EDITOR);
			}
		});
		
		Button editScheduleButton = (Button)findViewById(R.id.mEditScheduleButton);
		editScheduleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EditSelector.this, ScheduleActivity.class); // launch screen2
				startActivityForResult(intent, LAUNCH_SCHEDULE_EDITOR);
			}
		});
		
		Button mAdvancedButton = (Button)findViewById(R.id.mAdvancedButton);
		mAdvancedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EditSelector.this, AdvancedActivity.class); // launch screen2
				startActivityForResult(intent, LAUNCH_ADVANCED_EDITOR);
			}
		});
		
		Button hacksButton = (Button)findViewById(R.id.mHacksButton);
		hacksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditSelector.this, HackActivity.class); // launch screen2
                startActivityForResult(intent, LAUNCH_HACK_ACTIVITY);
            }
        });
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if( requestCode == LAUNCH_PLAYER_EDITOR ) //&& resultCode == Activity.RESULT_OK )
		{
			Toast.makeText(this, "Player data saved.", Toast.LENGTH_LONG).show();
		}
		else if( requestCode == LAUNCH_TEAM_EDITOR  )
		{
			// it's confirmed ;)
			Toast.makeText(this, "Team data saved.", Toast.LENGTH_LONG).show();
		}
		else if( requestCode == LAUNCH_SCHEDULE_EDITOR )
		{
				Vector<String> errors = MainActivity.CurrentInputParser.GetAndResetErrors();
				if (errors.size() > 0) 
				{
					StringBuilder error = new StringBuilder(200);
					for (int i = 0; i < errors.size(); i++) 
					{
						error.append(errors.get(i));
						error.append("\n");
					}
					AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);                      
		    	    dlgAlert.setTitle("ERROR!"); 
		    	    dlgAlert.setMessage(error); 
		    	    
		    	    dlgAlert.setCancelable(true);
		    	    dlgAlert.create().show();
				}
				else
				{
					String message = "Schedule saved successfully";
					Toast.makeText(this, message, Toast.LENGTH_LONG).show();
				}
		}

		//Toast.makeText(this, "It's Confirmed! You're an ass!", Toast.LENGTH_LONG).show();
		super.onActivityResult(requestCode, resultCode, data);
	}
	
}
