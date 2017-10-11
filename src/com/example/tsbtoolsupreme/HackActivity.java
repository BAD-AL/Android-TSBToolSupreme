package com.example.tsbtoolsupreme;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class HackActivity extends Activity {

	private ListView hackList;
	private TextView pathTextBox;
	private Button applyButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hack);
		pathTextBox = (TextView) findViewById(R.id.mHackDir);
		hackList = (ListView) findViewById(R.id.mHackList);
		applyButton = (Button) findViewById(R.id.mAplyToRomButton);
		hackList.setChoiceMode(2);// multiple choice
		applyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onApplyClicked();
			}
		});
		pathTextBox.setText(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/TSB_Tool/HACKS/");

		populateHackList();
	}

	private void populateHackList() {

		File hackDir = new File(pathTextBox.getText() + "");
		if (hackDir.exists()) {
			
			HackItem current = null;
			String path = "";
			String content = "";
			ArrayList<HackItem> items = new ArrayList<HackItem>(20);
			File[] allFiles = hackDir.listFiles();
			for (int i = 0; i < allFiles.length; i++) {
				if (allFiles[i].getAbsolutePath().toLowerCase()
						.endsWith(".txt")) {
					path = allFiles[i].getAbsolutePath();
					content = readFile(allFiles[i]);
					current = new HackItem(path, content);
					items.add(current);
				}
			}
			ArrayAdapter<HackItem> hackAdapter = new ArrayAdapter<HackItem>(
					this, R.id.mHackList, items);
			hackList.setAdapter(hackAdapter);
		}
	}

	private String readFile(File f) {
		String retVal = "";
		if (f != null && f.exists()) {
			StringBuilder builder = new StringBuilder(500);
			try {
				BufferedReader reader = new BufferedReader(new FileReader(
						f.getAbsolutePath()));
				String line = "";
				while ((line = reader.readLine()) != null) {
					builder.append(line);
					builder.append("\n");
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			retVal = builder.toString();
		}
		return retVal;
	}

	private void onApplyClicked() {
		SparseBooleanArray choices = hackList.getCheckedItemPositions();
		StringBuilder builder = new StringBuilder(1000);
		ArrayAdapter<HackItem> hackAdapter = (ArrayAdapter<HackItem>) hackList.getAdapter();

		for (int i = 0; i < choices.size(); i++) {
			if (choices.get(i)) {
				builder.append(hackAdapter.getItem(i).getContent());
			}
		}
		if (builder.length() > 0) {
			BufferedReader reader = new BufferedReader(new StringReader(
					builder.toString()));
			Vector<String> lines = new Vector<String>(100);
			String line = "";
			try {
				while ((line = reader.readLine()) != null) {
					lines.add(line);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MainActivity.CurrentInputParser.ProcessLines(lines);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.hack, menu);
		return true;
	}

}
