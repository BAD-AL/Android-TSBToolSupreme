package com.example.tsbtoolsupreme;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class EditTeamActivity extends Activity 
{
	EditText simOffense;
	EditText simDefense;
	Spinner runPassRatioSpinner;
	Spinner formationSpinner;
	Spinner teamsSpinner;

	private ImageButton r1Button;
	private ImageButton r2Button;
	private ImageButton r3Button;
	private ImageButton r4Button;

	private ImageButton p1Button;
	private ImageButton p2Button;
	private ImageButton p3Button;
	private ImageButton p4Button;
	
	private boolean modified = false;
	private boolean initializing = false;
	
	private String[] formations = new String[] {
			  "2RB_2WR_1TE", "1RB_4WR", "1RB_3WR_1TE"
	  };
	private String[] ratios = new String[]{
			  "Balance Rush", "Heavy Rushing", 
			  "Balance Pass", "Heavy Pass"
	  };
	private NumberFilter filter0_15 = new NumberFilter(0, 15);
	
	private String currentTeamString ="";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		initializing = true;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_team_layout);
		
		  simOffense = (EditText)findViewById(R.id.mSimOffenseTextBox);
		  simDefense = (EditText)findViewById(R.id.mSimDefenseTextBox);
		  
		  r1Button = (ImageButton)findViewById(R.id.mR1Button);
		  r2Button = (ImageButton)findViewById(R.id.mR2Button);
		  r3Button = (ImageButton)findViewById(R.id.mR3Button);
		  r4Button = (ImageButton)findViewById(R.id.mR4Button);

		  p1Button = (ImageButton)findViewById(R.id.mP1Button);
		  p2Button = (ImageButton)findViewById(R.id.mP2Button);
		  p3Button = (ImageButton)findViewById(R.id.mP3Button);
		  p4Button = (ImageButton)findViewById(R.id.mP4Button);
		  
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
			
		  teamsSpinner = populateSpinner(R.id.mTeamsSpinner, teams);
		  formationSpinner  = populateSpinner(R.id.mFormationSpinner, formations);
		  runPassRatioSpinner  = populateSpinner(R.id.mRunPassRatioSpinner, ratios);
		  simOffense.setFilters(new InputFilter[] { filter0_15 });
		  simDefense.setFilters(new InputFilter[] { filter0_15 });
		  
		  // **************** Add Listeners *********************
		  addListeners();
		  setupCurrentTeam();
		 initializing = false;
	}
	
	private void addListeners()
	{
		  r1Button.setOnClickListener(buttonListener); 
		  r2Button.setOnClickListener(buttonListener);
		  r3Button.setOnClickListener(buttonListener);
		  r4Button.setOnClickListener(buttonListener);

		  p1Button.setOnClickListener(buttonListener);
		  p2Button.setOnClickListener(buttonListener);
		  p3Button.setOnClickListener(buttonListener);
		  p4Button.setOnClickListener(buttonListener);
		  
		  simOffense.addTextChangedListener(textWatcher);
		  simDefense.addTextChangedListener(textWatcher);
		  formationSpinner.setOnItemSelectedListener(spinListener);
		  runPassRatioSpinner.setOnItemSelectedListener(spinListener);
		  
		  teamsSpinner.setOnItemSelectedListener(teamSpinnerListener);
	}
	
	private void removeListeners()
	{
		  r1Button.setOnClickListener(null); 
		  r2Button.setOnClickListener(null);
		  r3Button.setOnClickListener(null);
		  r4Button.setOnClickListener(null);

		  p1Button.setOnClickListener(null);
		  p2Button.setOnClickListener(null);
		  p3Button.setOnClickListener(null);
		  p4Button.setOnClickListener(null);
		  
		  simOffense.removeTextChangedListener(textWatcher);
		  simDefense.removeTextChangedListener(textWatcher);
		  formationSpinner.setOnItemSelectedListener(null);
		  runPassRatioSpinner.setOnItemSelectedListener(null);
		  
		  teamsSpinner.setOnItemSelectedListener(null);
	}
	
	private Spinner populateSpinner(int id, String[] data) {
		Spinner spinner = (Spinner) findViewById(id);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, data);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		return spinner;
	}
	
	private Pattern teamPattern = Pattern.compile("TEAM\\s*=\\s*([0-9a-z]+)");
	private Pattern simPattern = Pattern.compile("SimData=0[xX]([0-9a-fA-F][0-9a-fA-F])([0-3]?)");
	private Pattern offensiveFormation = Pattern.compile("OFFENSIVE_FORMATION\\s*=\\s*([a-zA-Z1234_]+)");
	private Pattern playbookPattern = Pattern.compile("PLAYBOOK (R[1-8]{4})\\s*,\\s*(P[1-8]{4})");
	
	
	//"TEAM = bills SimData=0x ab 0, OFFENSIVE_FORMATION = 2RB_2WR_1TE"
	//"PLAYBOOK R1215, P7131 "
	private void setupCurrentTeam() 
	{
		removeListeners();
		String team = teamsSpinner.getSelectedItem().toString();
		try
		{
			String formation = MainActivity.CurrentTool.GetTeamOffensiveFormation(team);
			int simData = MainActivity.CurrentTool.GetTeamSimData(team) & 0xff;
			int offPref = MainActivity.CurrentTool.GetTeamSimOffensePref(team);
			String playbook = MainActivity.CurrentTool.GetPlaybook(team);
			
			// set 
			runPassRatioSpinner.setSelection(offPref);
			
			int index = MainActivity.GetIndex(formations, formation) ;
			formationSpinner.setSelection(index );
			int lower_byte = simData & 0x0F;
			int higher_byte = simData >> 4;
			simOffense.setText(higher_byte+"");
			simDefense.setText(""+lower_byte);
			
			// set playbooks
			index = playbook.indexOf('R');
			r1Button.setTag("r1_"+(char)(playbook.charAt(index +1)-1));
			r2Button.setTag("r2_"+(char)(playbook.charAt(index +2)-1));
			r3Button.setTag("r3_"+(char)(playbook.charAt(index +3)-1));
			r4Button.setTag("r4_"+(char)(playbook.charAt(index +4)-1));
			
			index = playbook.indexOf('P', 5);
			p1Button.setTag("p1_"+(char)(playbook.charAt(index +1)-1));
			p2Button.setTag("p2_"+(char)(playbook.charAt(index +2)-1));
			p3Button.setTag("p3_"+(char)(playbook.charAt(index +3)-1));
			p4Button.setTag("p4_"+(char)(playbook.charAt(index +4)-1));
			
			int r1d = getResources().getIdentifier(r1Button.getTag().toString(), "drawable", this.getPackageName());
			int r2d = getResources().getIdentifier(r2Button.getTag().toString(), "drawable", this.getPackageName());
			int r3d = getResources().getIdentifier(r3Button.getTag().toString(), "drawable", this.getPackageName());
			int r4d = getResources().getIdentifier(r4Button.getTag().toString(), "drawable", this.getPackageName());
			
			int p1d = getResources().getIdentifier(p1Button.getTag().toString(), "drawable", this.getPackageName());
			int p2d = getResources().getIdentifier(p2Button.getTag().toString(), "drawable", this.getPackageName());
			int p3d = getResources().getIdentifier(p3Button.getTag().toString(), "drawable", this.getPackageName());
			int p4d = getResources().getIdentifier(p4Button.getTag().toString(), "drawable", this.getPackageName());
			
			r1Button.setImageResource(r1d);
			r2Button.setImageResource(r2d);
			r3Button.setImageResource(r3d);
			r4Button.setImageResource(r4d);
			
			p1Button.setImageResource(p1d);
			p2Button.setImageResource(p2d);
			p3Button.setImageResource(p3d);
			p4Button.setImageResource(p4d);
			
		}
		catch(Exception e)
		{
		}
		addListeners();
	}
	
	// returns a string like:
	// "TEAM = bills SimData=0xab0, OFFENSIVE_FORMATION = 2RB_2WR_1TE
	//  PLAYBOOK R1234, P1234"
	private String getUITeamString()
	{
		StringBuilder ret = new StringBuilder(50);
		if( formationSpinner.getSelectedItem() != null && teamsSpinner.getSelectedItem() != null )
		{
			ret.append("TEAM = ");
			ret.append(teamsSpinner.getSelectedItem().toString());
			ret.append(" SimData=0x");
			ret.append(String.format("%x", Integer.parseInt(simOffense.getText().toString())));
			ret.append(String.format("%x", Integer.parseInt(simDefense.getText().toString())));
			ret.append(MainActivity.GetIndex(ratios, runPassRatioSpinner.getSelectedItem().toString()));
			ret.append(", OFFENSIVE_FORMATION = ");
			ret.append(formationSpinner.getSelectedItem().toString());
			ret.append("\n");
			
			ret.append("PLAYBOOK R");
			ret.append(Integer.parseInt(""+r1Button.getTag().toString().charAt(3))+1);
			ret.append(Integer.parseInt(""+r2Button.getTag().toString().charAt(3))+1);
			ret.append(Integer.parseInt(""+r3Button.getTag().toString().charAt(3))+1);
			ret.append(Integer.parseInt(""+r4Button.getTag().toString().charAt(3))+1);
	
			ret.append(", P");
			ret.append(Integer.parseInt(""+p1Button.getTag().toString().charAt(3))+1);
			ret.append(Integer.parseInt(""+p2Button.getTag().toString().charAt(3))+1);
			ret.append(Integer.parseInt(""+p3Button.getTag().toString().charAt(3))+1);
			ret.append(Integer.parseInt(""+p4Button.getTag().toString().charAt(3))+1);
		}
		return ret.toString();
	}
	
	private void onModified()
	{
		modified = true;
		currentTeamString = getUITeamString();
	}
	
	private void saveteamIfModified()
	{
		if( modified && !initializing )
			saveTeam();
		modified = false;
	}

	private void saveTeam() {
		String[] lines =currentTeamString.split("\n");
		
		try {
			for(int i =0; i < lines.length; i++)
			{
				MainActivity.CurrentInputParser.ProcessLine(lines[i]);
			}
		} catch (Exception e) {
			Toast.makeText(this, "ERROR!" + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
		Vector<String> errors = MainActivity.CurrentInputParser.GetAndResetErrors();
		if (errors.size() > 0) {
			StringBuilder error = new StringBuilder(200);
			for (int i = 0; i < errors.size(); i++) {
				error.append(errors.get(i));
				error.append("\n");
			}
			Toast.makeText(this, "ERROR!" + error, Toast.LENGTH_LONG).show();
		}
	}
	private static final int PLAY_PICKER = 4;
	
	/**
	 * Argument passe to the PlaySelectActivity (will be either 'r' or 'p')
	 */
	public static final String PASS_RUN = "pass_run";
	
	private void launchPlayPicker(String pass_run, int slot)
	{
		Intent intent = new Intent( EditTeamActivity.this, PlaySelectActivity.class);
		intent.putExtra(PASS_RUN, pass_run);
		intent.putExtra("slot", slot);
		startActivityForResult(intent, PLAY_PICKER);
	}
	
	ImageButton clickedButton = null;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if( requestCode == PLAY_PICKER && resultCode == Activity.RESULT_OK )
		{
			// it's confirmed ;)
			Object result  = data.getExtras().get("imageName");
			if( result != null && clickedButton != null)
			{
				String imageName = result.toString();
				int id = getResources().getIdentifier(imageName, "drawable",
						this.getPackageName());
				clickedButton.setImageResource(id);
				clickedButton.setTag(imageName);
				onModified();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	//*********************** Listeners ******************************
	// used for the play buttons 
	View.OnClickListener buttonListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) 
		{
			clickedButton = (ImageButton) v;
			String tag = v.getTag().toString();
			launchPlayPicker(tag.charAt(0)+"",  Integer.parseInt( ""+tag.charAt(1)) ) ;
		}
	};
	
	
	private AdapterView.OnItemSelectedListener teamSpinnerListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			setupCurrentTeam();
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {}
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
}
