package com.example.tsbtoolsupreme;

import java.util.Arrays;
import java.util.Vector;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AdvancedActivity extends Activity {

	EditText mContentsTextBox ;
	Button mViewContentsButton;
	Button mAplyToRomButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_advanced);
		
		mContentsTextBox = (EditText) findViewById(R.id.mHackList);
		mViewContentsButton =(Button)findViewById(R.id.mViewContentsButton);
		mAplyToRomButton =(Button)findViewById(R.id.mAplyToRomButton);
		MainActivity.CurrentTool.setShowOffPref(true);
		
		TecmoTool.ShowColors = TecmoTool.ShowPlaybook = TecmoTool.ShowTeamFormation = true;
		
		mViewContentsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					mContentsTextBox.setText(MainActivity.CurrentTool.GetAll());
				}
				catch(Exception e)
				{
					mContentsTextBox.setText(e.getMessage());
				}
			}
		});
		
		mAplyToRomButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					Vector<String> lines = new Vector<String>(
							Arrays.asList(mContentsTextBox.getText().toString().split("\n")));
					MainActivity.CurrentInputParser.ProcessLines(lines);
				}
				catch(Exception e)
				{
					//mContentsTextBox.setText(e.getMessage());
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.advanced, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch(item.getItemId())
		{
			case R.id.mShowColorsMenuItem:
				TecmoTool.ShowColors = !TecmoTool.ShowColors;
				break;
			case R.id.mShowPlaybooksMenuItem:
				TecmoTool.ShowPlaybook = !TecmoTool.ShowPlaybook ;
				break;
			case R.id.mShowTeamFormationMenuItem:
				TecmoTool.ShowTeamFormation = !TecmoTool.ShowTeamFormation;
				break;
			case R.id.mAdvancedSelectAllMenuItem:
				mContentsTextBox.selectAll();
				break;
			case R.id.mAdvancedCopyMenuItem:
				if( mContentsTextBox.getSelectionStart() != mContentsTextBox.getSelectionEnd())
				{
					int startSelection=mContentsTextBox.getSelectionStart();
					int endSelection=mContentsTextBox.getSelectionEnd();
					String selectedText = mContentsTextBox.getText().toString().substring(startSelection, endSelection);
					MainActivity.ClipboardManager.setText(selectedText);
				}
				break;
			case R.id.mAdvancedPasteMenuItem:
				if( MainActivity.ClipboardManager.hasText())
				{
					StringBuilder builder = new StringBuilder( mContentsTextBox.getText().length() + 80);
					builder.append(mContentsTextBox.getText().subSequence(0, mContentsTextBox.getSelectionStart()));
					builder.append( MainActivity.ClipboardManager.getText() );
					int caretPos = builder.length();
					builder.append(mContentsTextBox.getText().subSequence(mContentsTextBox.getSelectionEnd(), mContentsTextBox.getText().length()));
					mContentsTextBox.setText(builder);
					mContentsTextBox.setSelection(caretPos, caretPos);
				}
				break;
			case R.id.mAdvancedCutMenuItem:
				if( mContentsTextBox.getSelectionStart() != mContentsTextBox.getSelectionEnd())
				{
					int startSelection=mContentsTextBox.getSelectionStart();
					int endSelection=mContentsTextBox.getSelectionEnd();
					String selectedText = mContentsTextBox.getText().toString().substring(startSelection, endSelection);
					MainActivity.ClipboardManager.setText(selectedText);
					
					StringBuilder builder = new StringBuilder( mContentsTextBox.getText().length() -selectedText.length()+1);
					builder.append(mContentsTextBox.getText().subSequence(0, mContentsTextBox.getSelectionStart()));
					builder.append(mContentsTextBox.getText().subSequence(mContentsTextBox.getSelectionEnd(), mContentsTextBox.getText().length()));
					mContentsTextBox.setText(builder);
				}
				break;
				
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	

}
