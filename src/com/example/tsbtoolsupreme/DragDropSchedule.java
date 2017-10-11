package com.example.tsbtoolsupreme;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class DragDropSchedule extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drag_drop_schedule);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.drag_drop_schedule, menu);
		return true;
	}

}
