package com.example.tsbtoolsupreme;

import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditPlayerActivity extends Activity {

	private Spinner teamSpinner;
	private Spinner posSpinner;
	private EditText firstNameTextBox;
	private EditText lastNameTextBox;
	private EditText jerseyNumberTextBox;
	private ImageButton faceButton;

	private TextView attr1Label;
	private TextView attr2Label;
	private Spinner attr1Spinner;
	private Spinner attr2Spinner;

	private TextView accLabel;
	private TextView apbLabel;
	private Spinner accSpinner;
	private Spinner apbSpinner;

	private EditText sim1TextBox;
	private EditText sim2TextBox;
	private EditText sim3TextBox;
	private EditText sim4TextBox;
	private TextView sim1Label;
	private TextView sim2Label;
	private TextView sim3Label;
	private TextView sim4Label;
	private NumberFilter filter0_15 = new NumberFilter(0, 15);
	private NumberFilter filter0_255 = new NumberFilter(0, 255);
	private boolean modified = false;
	private boolean initializing = true;
	
	private String currentPlayerString = "";

	private Vector<Spinner> mAttributeSpinners = new Vector<Spinner>(8);
	private Vector<EditText> mSimVector = new Vector<EditText>(4);
	private String[] mAttributeSelections = new String[] { "6", "13", "19",
			"25", "31", "38", "44", "50", "56", "63", "69", "75", "81", "88",
			"94", "100" };
	
	private static final int FACE_PICKER = 0xFACE; 

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_player_layout);

		if (MainActivity.CurrentTool != null) {
			MainActivity.CurrentInputParser = new InputParser(MainActivity.CurrentTool);
			String[] teams = TecmoTool.Teams;
			int nfcIndx = MainActivity.GetIndex(TecmoTool.Teams, "NFC");
			int afcIndx = MainActivity.GetIndex(TecmoTool.Teams, "AFC");
			if( nfcIndx > 0 && afcIndx > 0)
			{
				int j = 0;
				teams = new String[TecmoTool.Teams.length-2];
				for(int i=0; i < TecmoTool.Teams.length; i++)
				{
					if( i != nfcIndx && i != afcIndx)
					{
						teams[j++] = TecmoTool.Teams[i];
					}
				}
			}
			populateSpinner(R.id.mTeamsSpinner, teams);
			populateSpinner(R.id.mPositionSpinner,
					MainActivity.CurrentTool.getPositionNames());
		}
		teamSpinner = (Spinner) findViewById(R.id.mTeamsSpinner);
		posSpinner = (Spinner) findViewById(R.id.mPositionSpinner);
		
		populateAttributeSpinners();
		// jersey number text box
		NumberFilter filter = new NumberFilter(0, 99);
		jerseyNumberTextBox = (EditText) findViewById(R.id.mPlayerNumberField);
		jerseyNumberTextBox.setFilters(new InputFilter[] { filter });

		// name text box
		firstNameTextBox = (EditText) findViewById(R.id.mHackDir);
		lastNameTextBox = (EditText) findViewById(R.id.mLastNameTextBox);

		faceButton = (ImageButton) findViewById(R.id.mFaceButton);
		faceButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				launchFacePicker();
			}
		});

		attr1Label = (TextView) findViewById(R.id.mAttr1Label);
		attr2Label = (TextView) findViewById(R.id.mAttr2Label);
		attr1Spinner = (Spinner) findViewById(R.id.mAtter1Spinner);
		attr2Spinner = (Spinner) findViewById(R.id.mAtter2Spinner);

		accSpinner = (Spinner) findViewById(R.id.mPassAccuracySpinner);
		apbSpinner = (Spinner) findViewById(R.id.mAvoidPassBlock);
		accLabel = (TextView) findViewById(R.id.mPassAccuracyLabel);
		apbLabel = (TextView) findViewById(R.id.mAvoidPassBlockLabel);

		sim1TextBox = (EditText) findViewById(R.id.mSim1TextBox);
		sim2TextBox = (EditText) findViewById(R.id.mSim2TextBox);
		sim3TextBox = (EditText) findViewById(R.id.mSim3TextBox);
		sim4TextBox = (EditText) findViewById(R.id.mSim4TextBox);
		sim1Label = (TextView) findViewById(R.id.mSim1Label);
		sim2Label = (TextView) findViewById(R.id.mSim2Label);
		sim3Label = (TextView) findViewById(R.id.mSim3Label);
		sim4Label = (TextView) findViewById(R.id.mSim4Label);

		mSimVector.add(sim1TextBox);
		mSimVector.add(sim2TextBox);
		mSimVector.add(sim3TextBox);
		mSimVector.add(sim4TextBox);

		addListeners();
		setupCurrentPlayer();
		// add change listeners
		
		teamSpinner.setOnItemSelectedListener(teamPosSpinnerListener);
		posSpinner.setOnItemSelectedListener(teamPosSpinnerListener);
		
	}
	
	private void addListeners()
	{
		for (int i = 0; i < mAttributeSpinners.size(); i++) {
			mAttributeSpinners.get(i).setOnItemSelectedListener(spinListener);
		}
	
		firstNameTextBox.addTextChangedListener(textWatcher);
		lastNameTextBox.addTextChangedListener(textWatcher);
		jerseyNumberTextBox.addTextChangedListener(textWatcher);
		sim1TextBox.addTextChangedListener(textWatcher);
		sim2TextBox.addTextChangedListener(textWatcher);
		sim3TextBox.addTextChangedListener(textWatcher);
		sim4TextBox.addTextChangedListener(textWatcher);
	}
	
	private void removeListeners()
	{
		for (int i = 0; i < mAttributeSpinners.size(); i++) {
			mAttributeSpinners.get(i).setOnItemSelectedListener(null);
		}
	
		firstNameTextBox.removeTextChangedListener(textWatcher);
		lastNameTextBox.removeTextChangedListener(textWatcher);
		jerseyNumberTextBox.removeTextChangedListener(textWatcher);
		sim1TextBox.removeTextChangedListener(textWatcher);
		sim2TextBox.removeTextChangedListener(textWatcher);
		sim3TextBox.removeTextChangedListener(textWatcher);
		sim4TextBox.removeTextChangedListener(textWatcher);
	}
	
	private void launchFacePicker()
	{
		Intent intent = new Intent(EditPlayerActivity.this, ChooseFaceActivity.class); // launch screen2
		//intent.putExtra(name, value);
		startActivityForResult(intent, FACE_PICKER);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if( requestCode == FACE_PICKER && resultCode == Activity.RESULT_OK )
		{
			// it's confirmed ;)
			Object result  = data.getExtras().get("imageName");
			if( result != null )
			{
				String faceString = result.toString(); //"face_00"
				int faceId = getResources().getIdentifier(faceString, "drawable", this.getPackageName());
				String hexNum = faceString.replace("face_", "");
				int face = Integer.parseInt(hexNum,16);
				faceButton.setImageResource(faceId);
				faceButton.setTag(face);
				onModified();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	/**
	 * Will save the current player if something has changed.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        savePlayerIfModified();
	    }
	    return super.onKeyDown(keyCode, event);
	}

	private String mCurrentTeam ="";
	
	private void setupCurrentPlayer() {
		
		removeListeners();
		modified = false;
		initializing = true;
		String team = teamSpinner.getSelectedItem().toString();
		String pos = posSpinner.getSelectedItem().toString();
		String playerData = ""; //
		SetupUIStateForPosition(pos);
		mCurrentTeam = team;

		try {
			playerData = MainActivity.CurrentTool.GetPlayerData(team, pos);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int[] attributes = MainActivity.CurrentInputParser.GetInts(playerData);
		int[] simVals = MainActivity.CurrentInputParser.GetSimVals(playerData);

		for (int i = 0; i < attributes.length; i++) {
			int position = MainActivity.GetIndex(mAttributeSelections, "" + attributes[i]);
			mAttributeSpinners.get(i).setSelection(position, false);
		}

		if (simVals != null) {
			for (int i = 0; i < simVals.length; i++) {
				mSimVector.get(i).setText(simVals[i] + "");
			}
		}

		String fName = MainActivity.CurrentInputParser.GetFirstName(playerData);
		String lName = MainActivity.CurrentInputParser.GetLastName(playerData);
		int jerseyNumber = MainActivity.CurrentInputParser.GetJerseyNumber(playerData);
		int face = MainActivity.CurrentInputParser.GetFace(playerData);

		firstNameTextBox.setText("");
		lastNameTextBox.setText("");
		jerseyNumberTextBox.setText("");

		firstNameTextBox.setText(fName);
		lastNameTextBox.setText(lName);
		jerseyNumberTextBox.setText(String.format("%x", jerseyNumber));
		jerseyNumberTextBox
				.setSelection(jerseyNumberTextBox.getText().length());

		// update face button
		String faceString = String.format("face_%02x", face);
		int faceId = getResources().getIdentifier(faceString, "drawable",
				this.getPackageName());
		// getResources().getIdentifier(faceString, null, null );
		// Drawable faceImage = getResources().getDrawable(faceId);
		faceButton.setImageResource(faceId);
		faceButton.setTag(face);
		initializing = false;
		addListeners();
	}
	
	
	private void onModified()
	{
		modified = true;
		currentPlayerString = getUIPlayerString();
	}
	
	private void savePlayerIfModified()
	{
		if( modified && !initializing )
			savePlayer();
		modified = false;
	}

	private void savePlayer() 
	{
		String teamLine = "TEAM = " + mCurrentTeam; //teamSpinner.getSelectedItem().toString();
		String playerLine = currentPlayerString; // getUIPlayerString();
		if (playerLine.startsWith("ERROR")) {
			Toast.makeText(this, playerLine, Toast.LENGTH_LONG).show();
		} else {
			try {
				MainActivity.CurrentInputParser.ProcessLine(teamLine);
				MainActivity.CurrentInputParser.ProcessLine(playerLine);
			} catch (Exception e) {
				Toast.makeText(this, "ERROR!" + e.getMessage(),
						Toast.LENGTH_LONG).show();
			}
			Vector<String> errors = MainActivity.CurrentInputParser.GetAndResetErrors();
			if (errors.size() > 0) {
				StringBuilder error = new StringBuilder(200);
				for (int i = 0; i < errors.size(); i++) {
					error.append(errors.get(i));
					error.append("\n");
				}
				Toast.makeText(this, "ERROR!" + error, Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	/**
	 * returns a string like:
	 * "QB1, qb BILLS, Face=0x52, #0, 25, 69, 13, 13, 56, 81, 81, 81 ,[3, 12, 3 ]"
	 * If an error was encountered, will return a string starting with "ERROR"
	 * 
	 * @return
	 */
	private String getUIPlayerString() {
		String error = "ERROR! "; // len = 7
		StringBuilder ret = new StringBuilder();
		String fName = firstNameTextBox.getText().toString().replace(" ", "");
		String lName = lastNameTextBox.getText().toString().replace(" ", "");
		String jersey = jerseyNumberTextBox.getText().toString();
		if (fName.length() < 1)
			fName = "blank";
		if (lName.length() < 1)
			lName = "BLANK";
		if (jersey.length() < 1)
			jersey = "0";

		ret.append(String.format("%s, %s %s, Face=0x%2x, #%s, ", posSpinner
				.getSelectedItem().toString(), fName, lName,
				(Integer) faceButton.getTag(), jersey));
		// add player attrs here...
		for (int i = 0; i < mAttributeSpinners.size(); i++) {
			if (mAttributeSpinners.get(i).isEnabled()
					&& mAttributeSpinners.get(i).getVisibility() == View.VISIBLE) {
				ret.append(mAttributeSpinners.get(i).getSelectedItem()
						.toString());
				ret.append(",");
			}
		}
		String simVal = "";
		if (mSimVector.get(0).getVisibility() == View.VISIBLE) {
			ret.append("[");
			for (int i = 0; i < mSimVector.size(); i++) {
				if (mSimVector.get(i).isEnabled()
						&& mSimVector.get(i).getVisibility() == View.VISIBLE) {
					simVal = mSimVector.get(i).getText().toString();
					if (simVal.length() < 1)
						simVal = "0";
					ret.append(simVal);
					ret.append(",");
				}

			}
			ret.delete(ret.length() - 1, ret.length());
			ret.append("]");
		}
		if (error.length() > 10) {
			error += "Change not applied";
			return error;
		}
		return ret.toString();
	}

	private void SetupUIStateForPosition(String pos) {

		if (pos.equals("QB1") || pos.equals("QB2")) {
			attr1Label.setText("PS");
			attr2Label.setText("PC");
			accLabel.setText("PA");
			apbLabel.setText("APB");
			attr1Spinner.setEnabled(true);
			attr1Spinner.setVisibility(View.VISIBLE);
			attr2Spinner.setEnabled(true);
			attr2Spinner.setVisibility(View.VISIBLE);
			accSpinner.setEnabled(true);
			accSpinner.setVisibility(View.VISIBLE);
			apbSpinner.setEnabled(true);
			apbSpinner.setVisibility(View.VISIBLE);
			// Sim stuff
			sim1TextBox.setEnabled(true);
			sim1TextBox.setVisibility(View.VISIBLE);
			sim2TextBox.setEnabled(true);
			sim2TextBox.setVisibility(View.VISIBLE);
			sim3TextBox.setEnabled(true);
			sim3TextBox.setVisibility(View.VISIBLE);
			sim4TextBox.setEnabled(false);
			sim4TextBox.setVisibility(View.INVISIBLE);
			sim1Label.setText("sim run");
			sim2Label.setText("sim pass");
			sim3Label.setText("sim pocket");
			sim4Label.setText("");

			sim1TextBox.setFilters(new InputFilter[] { filter0_15 });
			sim2TextBox.setFilters(new InputFilter[] { filter0_15 });
			sim3TextBox.setFilters(new InputFilter[] { filter0_15 });
			sim1TextBox.setHint("0-15");
			sim2TextBox.setHint("0-15");
			sim3TextBox.setHint("0-15");
		} else if (pos.equals("RB1") || pos.equals("RB2") || pos.equals("RB3")
				|| pos.equals("RB4") || pos.equals("WR1") || pos.equals("WR2")
				|| pos.equals("WR3") || pos.equals("WR4") || pos.equals("TE1")
				|| pos.equals("TE2")) {
			attr1Label.setText("BC");
			attr2Label.setText("REC");
			accLabel.setText("");
			apbLabel.setText("");
			attr1Spinner.setEnabled(true);
			attr1Spinner.setVisibility(View.VISIBLE);
			attr2Spinner.setEnabled(true);
			attr2Spinner.setVisibility(View.VISIBLE);
			accSpinner.setEnabled(false);
			accSpinner.setVisibility(View.INVISIBLE);
			apbSpinner.setEnabled(false);
			apbSpinner.setVisibility(View.INVISIBLE);
			// Sim stuff
			sim1TextBox.setEnabled(true);
			sim1TextBox.setVisibility(View.VISIBLE);
			sim2TextBox.setEnabled(true);
			sim2TextBox.setVisibility(View.VISIBLE);
			sim3TextBox.setEnabled(true);
			sim3TextBox.setVisibility(View.VISIBLE);
			sim4TextBox.setEnabled(true);
			sim4TextBox.setVisibility(View.VISIBLE);
			sim1Label.setText("sim run");
			sim2Label.setText("sim catch");
			sim3Label.setText("sim PR");
			sim4Label.setText("sim KR");

			sim1TextBox.setFilters(new InputFilter[] { filter0_15 });
			sim2TextBox.setFilters(new InputFilter[] { filter0_15 });
			sim3TextBox.setFilters(new InputFilter[] { filter0_15 });
			sim4TextBox.setFilters(new InputFilter[] { filter0_15 });
			sim1TextBox.setHint("0-15");
			sim2TextBox.setHint("0-15");
			sim3TextBox.setHint("0-15");
			sim4TextBox.setHint("0-15");
		} else if (pos.equals("RG") || pos.equals("LG") || pos.equals("C")
				|| pos.equals("RT") || pos.equals("LT")) {
			attr1Label.setText("");
			attr2Label.setText("");
			accLabel.setText("");
			apbLabel.setText("");
			attr1Spinner.setEnabled(false);
			attr1Spinner.setVisibility(View.INVISIBLE);
			attr2Spinner.setEnabled(false);
			attr2Spinner.setVisibility(View.INVISIBLE);
			accSpinner.setEnabled(false);
			accSpinner.setVisibility(View.INVISIBLE);
			apbSpinner.setEnabled(false);
			apbSpinner.setVisibility(View.INVISIBLE);
			// Sim stuff
			sim1TextBox.setEnabled(false);
			sim1TextBox.setVisibility(View.INVISIBLE);
			sim2TextBox.setEnabled(false);
			sim2TextBox.setVisibility(View.INVISIBLE);
			sim3TextBox.setEnabled(false);
			sim3TextBox.setVisibility(View.INVISIBLE);
			sim4TextBox.setEnabled(false);
			sim4TextBox.setVisibility(View.INVISIBLE);
			sim1Label.setText("");
			sim2Label.setText("");
			sim3Label.setText("");
			sim4Label.setText("");
		} else if (pos.equals("LE") || pos.equals("RE") || pos.equals("NT")
				|| pos.equals("LOLB") || pos.equals("LILB")
				|| pos.equals("RILB") || pos.equals("ROLB")
				|| pos.equals("LCB") || pos.equals("RCB") || pos.equals("FS")
				|| pos.equals("SS")) {
			attr1Label.setText("PI");
			attr2Label.setText("QU");
			accLabel.setText("");
			apbLabel.setText("");
			attr1Spinner.setEnabled(true);
			attr1Spinner.setVisibility(View.VISIBLE);
			attr2Spinner.setEnabled(true);
			attr2Spinner.setVisibility(View.VISIBLE);
			accSpinner.setEnabled(false);
			accSpinner.setVisibility(View.INVISIBLE);
			apbSpinner.setEnabled(false);
			apbSpinner.setVisibility(View.INVISIBLE);
			// Sim stuff
			sim1TextBox.setEnabled(true);
			sim1TextBox.setVisibility(View.VISIBLE);
			sim2TextBox.setEnabled(true);
			sim2TextBox.setVisibility(View.VISIBLE);
			sim3TextBox.setEnabled(false);
			sim3TextBox.setVisibility(View.INVISIBLE);
			sim4TextBox.setEnabled(false);
			sim4TextBox.setVisibility(View.INVISIBLE);
			sim1Label.setText("sim pass rush");
			sim2Label.setText("sim cover");
			sim3Label.setText("");
			sim4Label.setText("");

			sim1TextBox.setFilters(new InputFilter[] { filter0_255 });
			sim2TextBox.setFilters(new InputFilter[] { filter0_255 });
			sim1TextBox.setHint("0-255");
			sim2TextBox.setHint("0-255");
		} else if (pos.equals("P") || pos.equals("K")) {
			attr1Label.setText("KA");
			attr2Label.setText("AKB");
			accLabel.setText("");
			apbLabel.setText("");
			attr1Spinner.setEnabled(true);
			attr1Spinner.setVisibility(View.VISIBLE);
			attr2Spinner.setEnabled(true);
			attr2Spinner.setVisibility(View.VISIBLE);
			accSpinner.setEnabled(false);
			accSpinner.setVisibility(View.INVISIBLE);
			apbSpinner.setEnabled(false);
			apbSpinner.setVisibility(View.INVISIBLE);
			// Sim stuff
			sim1TextBox.setEnabled(true);
			sim1TextBox.setVisibility(View.VISIBLE);
			sim2TextBox.setEnabled(false);
			sim2TextBox.setVisibility(View.INVISIBLE);
			sim3TextBox.setEnabled(false);
			sim3TextBox.setVisibility(View.INVISIBLE);
			sim4TextBox.setEnabled(false);
			sim4TextBox.setVisibility(View.INVISIBLE);
			sim1Label.setText("sim KA");
			sim2Label.setText("");
			sim3Label.setText("");
			sim4Label.setText("");
			sim1TextBox.setFilters(new InputFilter[] { filter0_15 });
			sim1TextBox.setHint("0-15");
		}
	}



	
	private void populateAttributeSpinners() {
		mAttributeSpinners
				.add(populateAttributeSpinner(R.id.mRunningSpeedSpinner));
		mAttributeSpinners
				.add(populateAttributeSpinner(R.id.mRushingPowerSpinner));
		mAttributeSpinners.add(populateAttributeSpinner(R.id.mMaxSpeedSpinner));
		mAttributeSpinners
				.add(populateAttributeSpinner(R.id.mHittingPowerSpinner));
		mAttributeSpinners.add(populateAttributeSpinner(R.id.mAtter1Spinner));
		mAttributeSpinners.add(populateAttributeSpinner(R.id.mAtter2Spinner));
		mAttributeSpinners
				.add(populateAttributeSpinner(R.id.mPassAccuracySpinner));
		mAttributeSpinners.add(populateAttributeSpinner(R.id.mAvoidPassBlock));
	}

	private Spinner populateAttributeSpinner(int id) {
		return populateSpinner(id, mAttributeSelections);
	}

	private Spinner populateSpinner(int id, String[] data) {
		Spinner spinner = (Spinner) findViewById(id);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, data);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		return spinner;
	}
	
	//*********************** Listeners ************************************************ 
	private AdapterView.OnItemSelectedListener teamPosSpinnerListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			savePlayerIfModified();
			setupCurrentPlayer();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};
	
	private OnItemSelectedListener spinListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parentView,
				View selectedItemView, int position, long id) {
			onModified();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parentView) {
		}
	};

	private TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			onModified();
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		@Override
		public void afterTextChanged(Editable s) {
		}
	};
	


}
