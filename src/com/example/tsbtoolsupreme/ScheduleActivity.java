package com.example.tsbtoolsupreme;

import java.util.Arrays;
import java.util.Vector;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent.OnFinished;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class ScheduleActivity extends Activity {

	EditText textBox;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule_layout);
		
		textBox = (EditText)findViewById(R.id.mScheduleTextBox);
		textBox.append("#Schedule\n");
		textBox.append(MainActivity.CurrentTool.GetSchedule());
		textBox.setSelection(0);
	}
	
	@Override
	protected void onDestroy() 
	{
		Vector<String> lines = new Vector<String>(
				Arrays.asList(textBox.getText().toString().split("\n")));
		MainActivity.CurrentInputParser.ProcessLines(lines);
		
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.schedule, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch(item.getItemId())
		{
			case R.id.mScheduleSelectAllMenuItem:
				textBox.selectAll();
				break;
			case R.id.mScheduleCopyMenuItem:
				if( textBox.getSelectionStart() != textBox.getSelectionEnd())
				{
					int startSelection=textBox.getSelectionStart();
					int endSelection=textBox.getSelectionEnd();
					String selectedText = textBox.getText().toString().substring(startSelection, endSelection);
					MainActivity.ClipboardManager.setText(selectedText);
				}
				break;
			case R.id.mSchedulePasteMenuItem:
				if( MainActivity.ClipboardManager.hasText())
				{
					StringBuilder builder = new StringBuilder( textBox.getText().length() + 80);
					builder.append(textBox.getText().subSequence(0, textBox.getSelectionStart()));
					builder.append( MainActivity.ClipboardManager.getText() );
					int caretPos = builder.length();
					builder.append(textBox.getText().subSequence(textBox.getSelectionEnd(), textBox.getText().length()));
					textBox.setText(builder);
					textBox.setSelection(caretPos, caretPos);
				}
				break;
			case R.id.mScheduleCutMenuItem:
				if( textBox.getSelectionStart() != textBox.getSelectionEnd())
				{
					int startSelection=textBox.getSelectionStart();
					int endSelection=textBox.getSelectionEnd();
					String selectedText = textBox.getText().toString().substring(startSelection, endSelection);
					MainActivity.ClipboardManager.setText(selectedText);
					
					StringBuilder builder = new StringBuilder( textBox.getText().length() -selectedText.length()+1);
					builder.append(textBox.getText().subSequence(0, textBox.getSelectionStart()));
					builder.append(textBox.getText().subSequence(textBox.getSelectionEnd(), textBox.getText().length()));
					textBox.setText(builder);
				}
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}

}
