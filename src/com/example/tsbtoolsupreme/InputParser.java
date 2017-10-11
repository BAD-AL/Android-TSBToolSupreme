package com.example.tsbtoolsupreme;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/// <summary>
/// Summary description for InputParser.
/// </summary>
public class InputParser
{
    private ITecmoTool tool;
    private final int scheduleState = 0;
    private final int rosterState   = 1;
    private int currentState = 2;
    public boolean showSimError= false;
    private Vector<String> errors = new Vector<String>();

    private static Pattern teamRegex, weekRegex, gameRegex, numberRegex, 
        posNameFaceRegex, simDataRegex, yearRegex, setRegex,
        returnTeamRegex, offensiveFormationRegex, playbookRegex,
        juiceRegex, homeRegex, awayRegex, divChampRegex, confChampRegex, uniformUsageRegex;

    private String currentTeam; //used for roster update
    private Vector<String> scheduleList;

    public InputParser(ITecmoTool tool)
    {
        this.tool = tool;
        currentTeam      = "bills";
        Init();
    }

    public InputParser()
    {
        currentTeam      = "bills";
        Init();
    }

    private static void Init()
    {
        if( numberRegex == null )
        {
//                currentTeam      = "bills";
            numberRegex      = Pattern.compile("(#[0-9]{1,2})");
            teamRegex    = Pattern.compile("TEAM\\s*=\\s*([0-9a-z]+)");
            simDataRegex     = Pattern.compile("SimData=0[xX]([0-9a-fA-F][0-9a-fA-F])([0-3]?)");
            weekRegex    = Pattern.compile("WEEK ([1-9][0    -7]?)");
            gameRegex    = Pattern.compile("([0-9a-z]+)\\s+at\\s+([0-9a-z]+)");
            posNameFaceRegex = Pattern.compile("([A-Z]+[1-4]?)\\s*,\\s*([a-zA-Z \\.\\-]+),\\s*(face=0[xX][0-9a-fA-F]+\\s*,\\s*)?");
            yearRegex    = Pattern.compile("YEAR\\s*=\\s*([0-9]+)");
            returnTeamRegex  = Pattern.compile("RETURN_TEAM\\s+([A-Z1-4]+)\\s*,\\s*([A-Z1-4]+)\\s*,\\s*([A-Z1-4]+)");
            setRegex     = Pattern.compile("SET\\s*\\(\\s*(0x[0-9a-fA-F]+)\\s*,\\s*(0x[0-9a-fA-F]+)\\s*\\)");
            offensiveFormationRegex = Pattern.compile("OFFENSIVE_FORMATION\\s*=\\s*([a-zA-Z1234_]+)");
            playbookRegex    = Pattern.compile("PLAYBOOK (R[1-8]{4})\\s*,\\s*(P[1-8]{4})");
            juiceRegex       = Pattern.compile("JUICE\\(\\s*([0-9]{1,2}|ALL)\\s*,\\s*([0-9]{1,2})\\s*\\)");
            homeRegex    = Pattern.compile("Uniform1\\s*=\\s*0x([0-9a-fA-F]{6})");
            awayRegex    = Pattern.compile("Uniform2\\s*=\\s*0x([0-9a-fA-F]{6})");
            divChampRegex    = Pattern.compile("DivChamp\\s*=\\s*0x([0-9a-fA-F]{10})");
            confChampRegex   = Pattern.compile("ConfChamp\\s*=\\s*0x([0-9a-fA-F]{8})");
            uniformUsageRegex= Pattern.compile("UniformUsage\\s*=\\s*0x([0-9a-fA-F]{8})");
        }
    }

    public void ProcessFile(String fileName)
    {
        try
        {
            BufferedReader reader = new BufferedReader( new FileReader(fileName));
            Vector<String> lines = new Vector<String>(300);
            String line="";
            while( (line = reader.readLine()) != null)
            {
                lines.add(line);
            }
            ProcessLines(lines);
        }
        catch(Exception e){
            MainClass.ShowError(e.getMessage());
        }    
    }

    public void ProcessLines(Vector<String> lines)
    {
        int i =0;
        try
        {
            for( i =0; i < lines.size(); i++)
            {
                ProcessLine(lines.get(i));
                //System.out.println(i);
            }
            ShowErrors();
            ApplySchedule();
        }
        catch(Exception e)
        {
            StringBuilder sb = new StringBuilder(150);
            sb.append( "Error! ");
            if( i < lines.size() )
                sb.append(String.format("line #%d:\t'%s'",i, lines.get(i)));
            sb.append(e.getMessage());
            sb.append("\n");
            sb.append(e.getStackTrace());
//                        "Error Processing line {0}:\t'{1}'.\n{2}\n{3}",
//                        i,lines[i], e.Message,e.StackTrace);
            sb.append("\n\nOperation aborted at this point. Data not applied.");
            MainClass.ShowError(sb.toString());
        }
    }

    private void ShowErrors()
    {
        if( tool.getErrors().size()> 0)
        {
            MainClass.ShowErrors(tool.getErrors());
            tool.setErrors( new Vector<String>());
        }
        if( errors.size()> 0 )
        {
            MainClass.ShowErrors(errors);
            errors = new Vector<String>();
        }
    }
    
    public Vector<String> GetAndResetErrors()
    {
    	Vector<String> ret = new Vector<String> (20);
    	
        if( tool.getErrors().size()> 0)
        {
            ret.addAll(tool.getErrors());
            tool.setErrors( new Vector<String>());
        }
        if( errors.size()> 0 )
        {
            ret.addAll(errors);
            errors = new Vector<String>();
        }
        return ret;
    }

    protected void ApplySchedule()
    {
        if( scheduleList != null )
        {
            Vector<String> errors =  tool.ApplySchedule(scheduleList);
            MainClass.ShowErrors( errors );
            scheduleList = null;
        }
    }



    public void ReadFromStdin()
    {
        String line= "";
        int lineNumber = 0;
        System.out.println("Reading from standard in...");
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            while( (line=br.readLine()) != null)
            {
                lineNumber++;
                ProcessLine(line);
                //System.out.println("Line "+lineNumber);
            }
            ShowErrors();
            ApplySchedule();
        }
        catch(Exception e)
        {
            MainClass.ShowError(String.format(
     "Error Processing line %d:'%d'.\n%s\n%s",
                lineNumber,line, e.getMessage(),e.getStackTrace()));
        }
    }

    /// <summary>
    /// 
    /// </summary>
    /// <param name="line"></param>
    protected void ProcessLine(String line) throws Exception
    {
        line = line.trim();
        Matcher juiceMatch; 

        if(line.startsWith("#") || line .equals("") || line.toLowerCase().trim().startsWith("schedule") )
            return;
        else if( /*setRegex.matcher(line) != Match.Empty )//*/
    line.startsWith("SET") )
        {
            tool.ApplySet(line);
        }
        else if(line.startsWith("PLAYBOOK"))
        {
            Matcher m = playbookRegex.matcher(line);
            if( m.find() )
            {
                String runs = m.group(1);
                String passes = m.group(2);
                tool.SetPlaybook(currentTeam, runs, passes);
            }
            else
            {
                errors.add(String.format("ERROR Setting playbook for team %s. Line '%d' is Invalid",
                    currentTeam, line));
            }
        }
            // JUICE( ALL, 17)
            // JUICE(1,17)
        else if( (juiceMatch = juiceRegex.matcher(line)).find() )
        {
            String juiceWeek  = juiceMatch.group(1).toString();
            int juiceAmt    = Integer.parseInt(juiceMatch.group(2));

            if( juiceWeek .equals("ALL") )
            {
                for(int i = 0; i < 17; i++)
                {
                    tool.ApplyJuice(i+1, juiceAmt);
                }
            }
            else 
            {
                int week = Integer.parseInt(juiceWeek)-1;
                if( !tool.ApplyJuice(week, juiceAmt))
                {
                    errors.add(String.format("ERROR! Line = '%s'",line));
                }
            }
        }
        else if(line.startsWith("COLORS")) // do the colors here
        {
            String tmp;

            Matcher home = homeRegex.matcher(line);
            Matcher away = awayRegex.matcher(line);
            Matcher confChamp = confChampRegex.matcher(line);
            Matcher divChamp = divChampRegex.matcher(line);
            Matcher uniUsage = uniformUsageRegex.matcher(line);
            if( home .find())
            {
                tmp = home.group(1);
                tool.SetHomeUniform(currentTeam, tmp);
            }
            if( away .find())
            {
                tmp = away.group(1);
                tool.SetAwayUniform(currentTeam, tmp);
            }
            if( confChamp .find())
            {
                tmp = confChamp.group(1);
                tool.SetConfChampColors(currentTeam, tmp);
            }
            if( divChamp .find())
            {
                tmp = divChamp.group(1);
                tool.SetDivChampColors(currentTeam, tmp);
            }
            if( uniUsage .find())
            {
                tmp = uniUsage.group(1);
                tool.SetUniformUsage(currentTeam, tmp);
            }
        }
        else if( teamRegex.matcher(line) .find())//line.startsWith("TEAM") )
        {
            System.out.printf("'%s' \n",line);
            currentState = rosterState;
            String team = GetTeam(line);
            boolean ret = SetCurrentTeam(team);
            if(!ret)
            {
                errors.add(String.format("ERROR with line '%s'.",line));
                errors.add(String.format("Team input must be in the form 'TEAM = team SimData=0x1F'"));
                return;
            }
            int[] simData = GetSimData(line);
            if( simData != null )
            {
                if(simData[0] > -1)
                    tool.SetTeamSimData(currentTeam,(byte)simData[0]);
                //else
                //    errors.add(String.format("Warning: No sim data for team %s",team));

                if( simData[1] > -1 )
                    tool.SetTeamSimOffensePref(currentTeam, simData[1]);
            }
            else
                errors.add(String.format("ERROR with line '%s'.",line));

            Matcher oFormPattern = offensiveFormationRegex.matcher(line);
            if( oFormPattern .find())
            {
                String formation = oFormPattern.group(1);
                tool.SetTeamOffensiveFormation( team, formation );
            }
        }
        else if( (weekRegex.matcher(line)).matches() )  //line.startsWith("WEEK"))
        {
            currentState = scheduleState;
            if( scheduleList == null)
                scheduleList = new Vector<String>(300);
            scheduleList.add( line );
        }
        else if( yearRegex.matcher(line) .matches())//line.startsWith("YEAR"))
        {
            SetYear(line);
        }
        else if(currentState == scheduleState)
        {
            if( scheduleList != null )
                scheduleList.add(line);
        }
        else if(currentState == rosterState)
        {
            UpdateRoster(line);
        }
        else
        {
            errors.add(String.format("Garbage/orphin line not applied \"%s\"", line));
        }
    }

    private void SetYear(String line)
    {
        Matcher m = yearRegex.matcher(line);
        m.find();
        String year = m.group(1);
        if(year.length() < 1)
        {
            errors.add(String.format("'%s' is not valid.",line));
        }
        else
        {
            tool.SetYear(year);
            System.out.printf("Year set to '%s'\n",year);
        }
    }

    private String GetTeam(String line)
    {
        Matcher m = teamRegex.matcher(line);
        m.find();
        String team = m.group(1);
        return team;
    }

    public int[] GetSimData(String line)
    {
    	int[] ret = {-1,-1};
        Matcher m = simDataRegex.matcher(line);
        if(m.find())
        {
	        //string data = m.Groups[2].ToString();
	        String data = m.group(1);
	        String simOffensePref = m.group(2);
	        
	        if(data.length() > 0)
	        {
	            try
	            {
	                int simData = Integer.parseInt(data,16);
	                ret[0]=simData;
	            }
	            catch(Exception e)
	            {
	                errors.add(String.format("Error getting SimData with line '%s'.",line));
	            }
	        }
	
	        if(simOffensePref.length() > 0)
	        {
	            try
	            {
	                int so = Integer.parseInt(simOffensePref);
	                ret[1] = so;
	            }
	            catch(Exception e )
	            {
	                errors.add(String.format("Error getting SimData with line '%s'.",line));
	            }
	        }
        }
        return ret;
    }

    private String GetAwayTeam(String line)
    {
        Matcher m = gameRegex.matcher(line);
        m.find();
        String awayTeam = m.group(1);
        return awayTeam;
    }

    private String GetHomeTeam(String line)
    {
        Matcher m = gameRegex.matcher(line);
        m.find();
        String team = m.group(2);
        return team;
    }

    private int GetWeek(String line)
    {
        Matcher m = weekRegex.matcher(line);
        m.find();
        String week_str = m.group(1);
        int ret = -1;
        try{
            ret = Integer.parseInt(week_str);
            ret--; // our week starts at 0
        }
        catch(Exception e){
            errors.add(String.format("Week '%s' is invalid.",week_str));
        }
        return ret;
    }

    private boolean SetCurrentTeam(String team)
    {
        if(TecmoTool.GetTeamIndex(team) < 0)
        {//error condition
            errors.add(String.format("Team '%s' is Invalid.",team));
            return false;
        }
        else
            this.currentTeam = team;
        return true;
    }

    protected void UpdateRoster(String line) throws Exception
    {
        if(line.startsWith("KR"))
            SetKickReturnMan(line);
        else if(line.startsWith("PR"))
            SetPuntReturnMan(line);
        else if(line.startsWith("RETURN_TEAM"))
        {
            Matcher m = returnTeamRegex.matcher(line);
            if( !m.find())
            {
                errors.add(String.format(
                    "Error with line '%s'.\n\tCorrect Syntax ='RETURN_TEAM POS1, POS2, POS3'",
                    line));
            }
            else
            {
                String pos1 = m.group(1);
                String pos2 = m.group(2);
                String pos3 = m.group(3);
                tool.SetReturnTeam(currentTeam, pos1,pos2,pos3);
            }
        }
        else
        {
            Matcher m = posNameFaceRegex.matcher( line );
            m.find();
            if( line.indexOf("#") > -1 )
            {
                if( !numberRegex.matcher(line) .find() )
                {
                    errors.add(String.format("ERROR! (jersey number) Line  %s",line));
                    return;
                }
            }
            String p = m.group(1);
            if( tool.IsValidPosition(p) )
            {
                if(line.startsWith("QB"))
                    SetQB(line);
                else if(line.startsWith("WR") || line.startsWith("RB") ||
                    line.startsWith("TE"))
                    SetSkillPlayer(line);
                else if(line.startsWith("C") || line.startsWith("RG") || 
                    line.startsWith("LG")    || line.startsWith("RT") ||
                    line.startsWith("LT"))
                {
                    SetOLPlayer(line);
                }
                else if(line.indexOf("LB") == 2 || line.indexOf("CB") == 1 ||
                    line.startsWith("RE") || line.startsWith("LE")  ||
                    line.startsWith("NT") || line.startsWith("SS")  ||
                    line.startsWith("FS") || line.startsWith("DB")    )
                {
                    SetDefensivePlayer(line);
                }
                else if( line.startsWith("P") || line.startsWith("K"))
                    SetKickPlayer(line);
            }
            else
            {
                errors.add(String.format("ERROR! With line \"%s\"     team = %s", line, currentTeam));
            }
        }
    }

    //QB1, chris MILLER, Face=0x33, #12, 25, 69, 13, 13, 31, 44, 50, 31 ,[2, 4, 3 ]

    private void SetQB(String line) throws Exception
    {
        String input = line.toLowerCase();
        //string simString = simRegex.matcher(line).Groups[1].ToString();
        String fname = GetFirstName(line);
        String lname = GetLastName(line);
        String pos = GetPosition(line);
        int face = GetFace(line);
        int jerseyNumber = GetJerseyNumber(line);//will be in hex, not base 10
        if(face > -1)
            tool.SetFace(currentTeam,pos,face);
        if( jerseyNumber < 0)
        {
            errors.add(String.format("Error with jersey number for '%s %s', setting to 0.",fname,lname));
            jerseyNumber=0;
        }
        tool.InsertPlayer(currentTeam,pos,fname,lname,(byte)jerseyNumber);

        int[] vals = GetInts(line);
        int[] simVals = GetSimVals(line);
        if(vals != null && vals.length > 7)
            tool.SetQBAbilities(currentTeam,pos,vals[0],vals[1],vals[2],vals[3],vals[4],vals[5],vals[6],vals[7]);
        else
            errors.add(String.format("Warning! could not set ability data for %s %s,",currentTeam,pos));
        if(face > -1)
            tool.SetFace(currentTeam,pos,face);
        if(simVals != null)
            tool.SetQBSimData(currentTeam,pos,simVals);
        else if(showSimError)
            errors.add(String.format("Warning! On line '%s'. No sim data specified.",line));
    }

    private void SetSkillPlayer(String line) throws Exception
    {
        String fname = GetFirstName(line);
        String lname = GetLastName(line);
        String pos = GetPosition(line);
        int face = GetFace(line);
        int jerseyNumber = GetJerseyNumber(line);//will be in hex, not base 10
        tool.SetFace(currentTeam,pos,face);
        tool.InsertPlayer(currentTeam,pos,fname,lname,(byte)jerseyNumber);

        int[] vals = GetInts(line);
        int[] simVals = GetSimVals(line);
        if(vals != null && vals.length > 5)
            tool.SetSkillPlayerAbilities(currentTeam,pos,vals[0],vals[1],vals[2],vals[3],vals[4],vals[5]);
        else
            errors.add(String.format("Warning! On line '%s'. No player data specified.",line));
        if(simVals!= null&& simVals.length > 3)
            tool.SetSkillSimData(currentTeam,pos,simVals);
        else  if(showSimError)
            errors.add(String.format("Warning! On line '%s'. No sim data specified.",line));
    }

    private void SetOLPlayer(String line) throws Exception
    {
        String fname = GetFirstName(line);
        String lname = GetLastName(line);
        String pos = GetPosition(line);
        int face = GetFace(line);
        int jerseyNumber = GetJerseyNumber(line);//will be in hex, not base 10
        int[] vals = GetInts(line);

        tool.SetFace(currentTeam,pos,face);
        tool.InsertPlayer(currentTeam,pos,fname,lname,(byte)jerseyNumber);

        if(vals != null && vals.length > 3)
            tool.SetOLPlayerAbilities(currentTeam,pos,vals[0],vals[1],vals[2],vals[3]);
        else
            errors.add(String.format("Warning! On line '%s'. No player data specified.",line));

    }

    protected void SetDefensivePlayer(String line) throws Exception
    {
        String fname = GetFirstName(line);
        String lname = GetLastName(line);
        String pos = GetPosition(line);
        int face = GetFace(line);
        int jerseyNumber = GetJerseyNumber(line);//will be in hex, not base 10
        int[] vals = GetInts(line);
        int[] simVals = GetSimVals(line);

        tool.SetFace(currentTeam,pos,face);
        tool.InsertPlayer(currentTeam,pos,fname,lname,(byte)jerseyNumber);

        if(vals != null && vals.length > 5)
            tool.SetDefensivePlayerAbilities(currentTeam,pos,vals[0],vals[1],vals[2],vals[3],vals[4],vals[5]);
        else
            errors.add(String.format("Warning! On line '%s'. Invalid player attributes.",line));
        if(simVals != null && simVals.length > 1)
            tool.SetDefensiveSimData(currentTeam,pos,simVals);
        else if(showSimError)
            errors.add(String.format("Warning! On line '%s'. No sim data specified.",line));
    }

    private void SetKickPlayer(String line) throws Exception
    {
        String fname = GetFirstName(line);
        String lname = GetLastName(line);
        String pos = GetPosition(line);
        int face = GetFace(line);
        int jerseyNumber = GetJerseyNumber(line);//will be in hex, not base 10
        int[] vals = GetInts(line);
        int[] simVals = GetSimVals(line);

        tool.SetFace(currentTeam,pos,face);
        tool.InsertPlayer(currentTeam,pos,fname,lname,(byte)jerseyNumber);
        if(vals != null && vals.length > 5)
            tool.SetKickPlayerAbilities(currentTeam,pos,vals[0],vals[1],vals[2],vals[3],vals[4],vals[5]);
        else
            errors.add(String.format("Warning! On line '%s'. No player data specified.",line));
        if(simVals != null && pos .equals("P"))
            tool.SetPuntingSimData(currentTeam, simVals[0]);
        else if(simVals != null && pos .equals("K"))
            tool.SetKickingSimData(currentTeam, simVals[0]);
        else if(showSimError)
            errors.add(String.format("Warning! On line '%s'. No sim data specified.",line));
    }

    private static Pattern KickRetMan = Pattern.compile("^KR\\s*,\\s*([A-Z1-4]+)$");
    private static Pattern PuntRetMan = Pattern.compile("^PR\\s*,\\s*([A-Z1-4]+)$");

    private void SetKickReturnMan(String line)
    {
        Matcher m = KickRetMan.matcher(line);
        if( m .find() )
        {
            String pos = m.group(1);
            if( tool.IsValidPosition( pos ) )
            {
                tool.SetKickReturner(currentTeam, pos);
            }
            else
                errors.add(String.format("ERROR with line '%s'.",line));
        }
    }

    private void SetPuntReturnMan(String line)
    {
        Matcher m = PuntRetMan.matcher(line);
        if( m .find())
        {
            String pos = m.group(1);
            if( tool.IsValidPosition( pos ) )
            {
                tool.SetPuntReturner(currentTeam, pos);
            }
            else
                errors.add(String.format("ERROR with line '%s'.",line));
        }
    }

    /// <summary>
    /// Expect line like '   [8, 9, 0 ]'
    /// </summary>
    /// <param name="input"></param>
    /// <returns></returns>
    public int[] GetSimVals(String input)
    {
        if( input != null )
        {
            String stuff = input.trim();
            int start = stuff.indexOf("[");
            int end = stuff.indexOf("]");
            if(start > -1 && end > -1)
            {
                stuff = stuff.substring(start+1,end);
                return GetInts(stuff);
            }
        }
        return null;
    }

    /**
     * Gets the Attributes from the String passed.
     * if passed:
     *  QB1, qb BILLS, Face=0x52, #0, 25, 69, 13, 13, 56, 81, 81, 81 ,[3, 12, 3 ]
     *  returns {25, 69, 13, 13, 56, 81, 81, 81}
     */
    public int[] GetInts(String input)
    {
        if( input != null )
        {
            int pound = input.indexOf("#");
            int brace = input.indexOf("[");
            if( pound > -1)
                input = input.substring(pound+3);
            if(brace > -1)
            {
                brace = input.indexOf("[");
                input = input.substring(0, brace);
            }
            //char[] seps = new char[] {' ',',','\t'};  //" ,\t".ToCharArray();
            String[] nums = input.split(" |,|\t");
            int j,count =0;
            for(j=0; j < nums.length; j++)
                if(nums[j].length() > 0)
                    count++;
            int[] result = new int[count];
            j = 0;

            String s ="";
            int i = 0;
            try
            {
                for( i = 0 ; i < nums.length; i++)
                {
                    s = nums[i];
                    if( s != null && s.length() > 0)
                        result[j++]=Integer.parseInt(s);
                }
                return result;
            }
            catch(Exception e)
            {
                String error =String.format("Error with input '%s', %s, was jersey number specified?",input,e.getMessage());
                errors.add(error);
                //System.Windows.Forms.MessageBox.Show(error);
            }
        }
        return null;
    }

    public int GetJerseyNumber(String line)
    {
        int ret = -1;
        Pattern jerseyRegex = Pattern.compile("#([0-9]+)");
        Matcher m = jerseyRegex.matcher(line);
        m.find();
        String num =  m.group(1);
        try
        {
            ret = Integer.parseInt(num,16);
        }
        catch(Exception e){ret = -1; }
        return ret;
    }

    public int GetFace(String line)
    {
        int ret = -1;
        Pattern hexRegex = Pattern.compile("0[xX]([A-Fa-f0-9]+)");
        Matcher m = hexRegex.matcher(line);
        if( m.find())
        {
            String num = m.group(1);
            try
            {
                ret = Integer.parseInt(num,16);
            }
            catch(Exception e )
            {
                ret = -1; 
                errors.add(String.format("Face ERROR line '%s'",line));
            }
        }

        return ret;
    }

    public String GetPosition(String line)
    {
        Matcher m = posNameFaceRegex.matcher(line);
        m.find();
        String pos = m.group(1);
        return pos;
    }

    public String GetLastName(String line)
    {
        String ret ="";
        Matcher m = posNameFaceRegex.matcher(line);
        if(m.find())
        {
            String name = m.group(2).trim();
            int index = name.lastIndexOf(" ");
            ret = name.substring(index+1);
        }
        return ret;
    }

    public String GetFirstName(String line)
    {
        String ret ="";
        Matcher m = posNameFaceRegex.matcher(line);
        if(m .find())
        {
            String name = m.group(2).trim();
            int index = name.lastIndexOf(" ");
            if( index > -1 && index < name.length() )
                ret = name.substring(0, index);
        }
        return ret;
    }


    /// <summary>
    /// 
    /// </summary>
    /// <param name="byteString">String in the format of a hex String (0123456789ABCDEF), must have
    /// an even number of characters.</param>
    /// <returns>The bytes.</returns>
    public static byte[] GetBytesFromString(String byteString)
    {
        byte[] ret = null;
        byte[] tmp = null;
        String b;
        if( byteString!= null && byteString.length() > 1 && (byteString.length() % 2) == 0)
        {
            tmp = new byte[byteString.length() /2];
            for(int i =0; i < tmp.length; i++)
            {
                b = byteString.substring(i*2,i*2+2);
                tmp[i] = (byte)Integer.parseInt(b, 16);
            }
            ret = tmp;
        }
        return ret;
    }

    public static String GetHomeUniformColorString(String line)
    {
        Init();
        String tmp = "";
        Matcher match = homeRegex.matcher(line);
        if( match.find())
        {
            tmp = match.group(1);
        }
        return tmp;
    }
    public static String GetAwayUniformColorString(String line)
    {
        Init();
        String tmp = "";
        Matcher match = awayRegex.matcher(line);
        if( match.find())
        {
            tmp = match.group(1);
        }
        return tmp;
    }
    public static String GetConfChampColorString(String line)
    {
        Init();
        String tmp = "";
        Matcher match = confChampRegex.matcher(line);
        if( match.find()  )
        {
            tmp = match.group(1);
        }
        return tmp;
    }
    public static String GetDivChampColorString(String line)
    {
        Init();
        String tmp = "";
        Matcher match = divChampRegex.matcher(line);
        if( match.find())
        {
            tmp = match.group(1);
        }
        return tmp;
    }

    public static String GetUniformUsageString(String line)
    {
        Init();
        String tmp = "";
        Matcher match = uniformUsageRegex.matcher(line);
        if( match.find())
        {
            tmp = match.group(1);
        }
        return tmp;
    }
    /// <summary>
    /// Returns the text String passed, without thr trailing commas.
    /// </summary>
    /// <param name="text"></param>
    /// <returns></returns>
    public static String DeleteTrailingCommas(String text)
    {
        String ret = text.replaceAll(",+\n", "\n");
        ret = ret.replaceAll(",+$", "");
        return ret;
    }

}
