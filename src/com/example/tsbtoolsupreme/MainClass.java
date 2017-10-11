package com.example.tsbtoolsupreme;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.DialogInterface;
//import javax.swing.JOptionPane;
import android.widget.Toast;


    /// <summary>
    /// Summary description for MainClass.
    /// </summary>
    public class MainClass
    {
        public static boolean GUI_MODE = false;
        public static String version = "Version 0.9.2 Java beta";

        //                   -j             -n     -f     -a         -s         -sch    
        private static boolean jerseyNumbers, names, faces, abilities, simData,  schedule, 
        //  -gui  -stdin
            gui,  stdin; 
        private static boolean printStuff, modifyStuff, printHelp;
        private static String outFileName = "output.nes";
        private static String getFileName = null;

        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        //[STAThread]
        public static void main(String[] stuff) throws NumberFormatException, IOException
        {
            //GUI_MODE = true;
            //MainGUI.PromptForSetUserInput("SET(0x2224B, {32TeamNES,28TeamNES PromptUser:Msg=\"Enter desired quarter length\":int(0x1-0x15)} )");

            jerseyNumbers = names = faces = abilities = simData =  
            printStuff = modifyStuff = schedule = false;
            //Junk(stuff);
            Vector<String> args = GetArgs(stuff);
            Vector<String> options = GetOptions(stuff);
            
            String romFile = GetRomFileName(args);
            if( romFile!= null&& romFile.toLowerCase().endsWith(".smc") )
            {
                outFileName = "output.smc";
            }
            else
            {
                outFileName = "output.nes";
            }
            SetupOptions(options);
            String dataFile = GetInputFileName(args);

            if(stuff.length == 0 || gui)
            {
                MainClass.GUI_MODE = true;
                //if (OnWindows)
                //    ShowWindow(GetConsoleWindow(), WindowShowStyle.Hide);
                //TODO: Launch main gui
                
                //Application.Run(new MainGUI(romFile, dataFile));
                //if (OnWindows)
                //    ShowWindow(GetConsoleWindow(), WindowShowStyle.Show);

                return;
            }
            if(printHelp )
            {
                PrintHelp();
                return;
            }

            if( stdin )
                dataFile = null; // if the romFile is null, we'll read from stdin.

            if(romFile == null)
                return;
              
            if( getFileName != null && ( new File(getFileName)).exists() )
            {
                ITecmoTool tool = TecmoToolFactory.GetToolForRom( romFile); 
                String result = GetLocations( getFileName, tool.getOutputRom());
                System.out.println(result);
                return;
            }
            if(options.size() == 0 && romFile != null )
            {
                printStuff = true;
                jerseyNumbers = names = faces = abilities = simData = printStuff = schedule = true;
            }
            else if(jerseyNumbers ||names ||faces ||abilities ||simData ||printStuff ||schedule || 
                TecmoTool.ShowColors || TecmoTool.ShowPlaybook || TecmoTool.ShowTeamFormation )
                printStuff = true;

            try
            {
                if(printStuff)
                    PrintStuff(romFile);
                else if(romFile != null && dataFile != null)
                    ModifyStuff(romFile,dataFile);
                else if(romFile != null)
                    ModifyStuff(romFile,null);
                else
                    System.err.println("Exiting...");
            }
            catch (Exception e)
            {
                System.err.println("ERROR!\n"+e.getMessage() + "\n"+e.getStackTrace());
            }
        }

        public static void PrintHelp()
        {
        //   -j             -n     -f     -a         -s   
        //    -sch    -gui  -stdin
            String message =  MainClass.version+

"Modifies and extracts info from Tecmo Superbowl nes ROM.\n\n"+

"Usage: TSBToolSupreme (<tecmorom.nes>|<tecmorom.smc>) [data file] [options]\n"+
"This program can extract data from a Tecmo Super Bowl rom (nes version only).\n\n"+

"The default behavior when called with a nes filename and no options is to print all player\n"+ 
"and schedule inormation from the given TSB rom file.\n\n"+

"When called with a TSB rom file and a data file, the behavior is that it will modify the TSB file\n"+
"with the data contained in the data file.\n\n"+

"The following are the available options.\n\n"+

"-j        Print jersey numbers.\n"+
"-n        Print player names.\n"+
"-f        Print player face attribute.\n"+
"-a        Print player abilities (running speed, rushing power, ...).\n"+
"-s        Print player sim data.\n"+
"-sch        Print schedule.\n"+
"-stdin        Read data from standard in.\n"+
"-gui        Launch GUI.\n"+
"-pb        Show Playbooks\n"+
"-of        Show Offensive Formations\n"+
"-colors        Show Uniform Colors\n"+
"-out:filename    Save modified rom to <filename>.\n"+
"-get:filename   Use <filename> as a 'GetBytes' file (get rom locations specified in file, print to stdout)\n";

            System.out.print(message);
        }

        private static Vector<String> GetOptions(String[] args)
        {
            Vector<String> ret = new Vector<String>();
            for(int i = 0 ; i < args.length; i++)
            {
                if(args[i].startsWith("-") || args[i].startsWith("/"))
                    ret.add(args[i]);
            }
            return ret;
        }

        private static Vector<String> GetArgs(String[] args)
        {
            Vector<String> ret = new Vector<String>();
            for(int i = 0 ; i < args.length; i++)
            {
                if(!args[i].startsWith("-") && !args[i].startsWith("/"))
                    ret.add(args[i]);
            }
            return ret;
        }

        private static void SetupOptions(Vector<String> options)
        {
            String option="";
            // player options = number,name,face,abilities, sim data
            for(int i =0;i < options.size(); i++)
            {
                option = options.get(i).toString().toLowerCase();
                if(option.startsWith("-out:") || option.startsWith("/out:") && option.length() > 5)
                {
                    String[] parts = option.split(":");
                    if(parts != null && parts.length > 1 && parts[1].length() > 1)
                        outFileName = parts[1];
                }
                else if( option.startsWith("-get:") || option.startsWith("/get:"))
                {
                    String[] parts = option.split(":");
                    if(parts != null && parts.length > 1 && parts[1].length() > 1)
                        getFileName = parts[1];
                }
                else
                {
                    if( option .equals("-j")|| option .equals("/j")){
                            jerseyNumbers=true;
                    }
                    else if( option .equals("-n")|| option .equals("/n")){
                            names = true;
                    }
                    else if( option .equals("-f")|| option .equals("/f")){
                            faces=true;
                    }
                    else if( option .equals("-a")|| option .equals("/a")){
                            abilities =true;
                    }
                    else if( option .equals("-s")|| option .equals("/s")){
                            simData=true; // for players
                    }
                    else if( option .equals("-sch")|| option .equals("/sch")){
                            schedule=true;
                    }
                    else if( option .equals("-h")|| option .equals("/h")|| 
                            option .equals("/?")|| option .equals("-?")){
                            printHelp=true;
                    }
                    else if( option .equals("-gui")|| option .equals("/gui")){
                            gui = true;
                    }
                    else if( option .equals("-stdin")|| option .equals("/stdin")){
                            stdin = true;
                    }
                    else if( option .equals("-pb")|| option .equals("/pb")){
                            TecmoTool.ShowPlaybook = true;
                            options.remove(option);
                            i--;
                    }
                    else if( option .equals("-of")|| option .equals("/of")){
                            TecmoTool.ShowTeamFormation = true;
                            i--;
                            options.remove(option);
                    }
                    else if( option .equals("-colors")|| option .equals("/colors")){
                            TecmoTool.ShowColors = true;
                    }
                    else {
                            System.err.println("Invalid option "+option);
                    }
                
                }
            }
            if( jerseyNumbers || names || faces || abilities || simData )
                printStuff = true;
        }

        private static String GetRomFileName(Vector<String> args)
        {
            String arg="";
            for(int i =0; i < args.size(); i++)
            {
                arg=args.get(i).toString().toLowerCase();
                if( (arg.endsWith(".nes")|| arg.endsWith(".smc")) && ! arg.startsWith("-out:"))
                    return args.get(i).toString();
            }
            System.err.println("No valid rom file passed as an argument.");
            return null;
        }

        private static String GetInputFileName(Vector<String> args)
        {
            String arg="";
            for(int i =0; i < args.size(); i++)
            {
                arg=args.get(i).toString().toLowerCase();
                if(!arg.endsWith(".nes")&& !arg.endsWith(".smc") )
                    return args.get(i).toString();
            }
            System.err.println("No valid input file passed as an argument.");
            return null;
        }

        private static void PrintStuff(String filename) throws Exception
        {
            ITecmoTool tool = TecmoToolFactory.GetToolForRom( filename); 
            if( tool == null )
            {
                ShowError("ERROR determining ROM type.");
                return;
            }
            String stuff = "";
            if(jerseyNumbers && names && faces && abilities && simData )
            {
                tool.setShowOffPref( true);
                stuff += tool.GetKey();
                stuff += tool.GetAll();
            }
            else if( TecmoTool.ShowColors || TecmoTool.ShowPlaybook || TecmoTool.ShowTeamFormation )
            {
                tool.setShowOffPref(true);
                stuff += tool.GetKey();
                stuff += tool.GetAll();
                //stuff = tool.GetPlayerStuff(jerseyNumbers,names,faces,abilities,simData);
            }
            else if(jerseyNumbers || names || faces || abilities || simData)
            {
                stuff = tool.GetPlayerStuff(jerseyNumbers,names,faces,abilities,simData);
            }
            if(schedule)
            {
                stuff += tool.GetSchedule();
            }

            if( File.pathSeparator  .equals("\\"))
            {
                stuff = stuff.replace("\r\n", "\n");
                stuff = stuff.replace("\n","\r\n");
            }
            System.out.println(stuff);
            //String playerStuff = tool.GetPlayerStuff();
        }

        public static void ModifyStuff(String romfile, String inputfile) throws IOException
        {
            ITecmoTool tt = TecmoToolFactory.GetToolForRom( romfile );

            InputParser parser  = new InputParser(tt);
            if(inputfile != null)
                parser.ProcessFile(inputfile);
            else
                parser.ReadFromStdin();
            tt.SaveRom(outFileName);
        }

        public static void ShowErrors( Vector<String> errors)
        {
            if( errors != null && errors.size() > 0 )
            {
                StringBuilder sb = new StringBuilder(500);

                int limit = errors.size();
                for(int i = 0; i < limit; i++)
                {
                    sb.append(errors.get(i) +"\n");
                }
                ShowError( sb.toString() );
            }
        }

        public static void ShowError( String error )
        {
            if( MainClass.GUI_MODE )
            {
            	//Toast.makeText(this, "It's Confirmed! You're an ass!", Toast.LENGTH_LONG).show();
            	/*AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(null);                      
            	    dlgAlert.setTitle("ERROR!"); 
            	    dlgAlert.setMessage(error); 
            	    
            	    dlgAlert.setCancelable(true);
            	    dlgAlert.create().show();*/
			// JOptionPane.showMessageDialog(null,  error, "Error!", JOptionPane.ERROR_MESSAGE);
            }
            else
                System.err.println( error );
        }

        /// <summary>
        /// Syntax:
        /// 0x123456789abcd - 0x23456712222
        /// </summary>
        /// <param name="fileName"></param>
        /// <returns></returns>
        public static String GetLocations(String fileName, byte[] rom ) throws NumberFormatException, IOException
        {
            String ret = "";
            int defaultWidth= 0x10;
            Pattern dude = Pattern.compile(
                    "0x([0123456789abcdefABCDEF]+)\\s*-\\s*0x([0123456789abcdefABCDEF]+)");
            
            Pattern dude2 = Pattern.compile(
"0x([0123456789abcdefABCDEF]+)\\s*-\\s*0x([0123456789abcdefABCDEF]+)\\s*,\\s*0x([0123456789abcdefABCDEF]+)");
            if( fileName != null && fileName.length() > 0)
            {
                StringBuilder sb = new StringBuilder( 500);
                //StreamReader reader = new StreamReader(fileName);
                BufferedReader reader = new BufferedReader(new FileReader(fileName));
                //String contents = reader.ReadToEnd().Replace("\r\n","\n");
                Matcher m;
                Matcher m2;
                //Match m,m2;
                String line = "";
                while ( (line = reader.readLine()) != null )
                {
                    if( (m2 = dude2.matcher(line)).find() )
                    {
                        long addr1 = Long.parseLong(m2.group(1), 16);
                        long addr2 = Long.parseLong(m2.group(2), 16);
                        int w      =  Integer.parseInt(m2.group(3),16);
                        if( addr1 <= addr2 )
                        {
                            sb.append(GetSetString(addr1, addr2, w ,rom));
                        }
                    }
                    else if( (m = dude.matcher(line)).find() )
                    {
                        long addr1 = Long.parseLong(m.group(1)    ,16 );
                        long addr2 = Long.parseLong(m.group(2)    , 16);
                        if( addr1 <= addr2 )
                        {
                            sb.append(GetSetString(addr1, addr2,defaultWidth ,rom));
                        }
                    }
                    else if( line.startsWith("#"))
                    {
                        sb.append(line);
                        sb.append("\n");
                    }
                    else
                    {
                        sb.append("#Problem with input line '");
                        sb.append(line);
                        sb.append("'\n");
                        sb.append("#correct format = <starting address>-<ending address>,byte width\n");
                        sb.append("#example: 0x12345-0x12355,0x10\n");
                    }
                }
                reader.close();
                ret = sb.toString();
            }
            return ret;
        }

        private static String GetSetString(long addr1, long addr2,int width,  byte[] rom)
        {
            String ret = null;
            if( rom == null )
            {
                ShowError("ERROR! rom is null.");
            }
            else if( addr2 < addr1  )
            {
                ShowError(String.format(
                    "ERROR! ending'0x%x' address greater than starting address'%x'.",
                    addr1, addr2));
            }
            else if(addr2 > rom.length )
            {
                ShowError(String.format(
                    "ERROR! ending address '0x%x'is out of range.",
                    addr2));
            }
            else
            {
                StringBuilder sb = new StringBuilder( ((int)(addr2-addr1))*2+100);
                boolean done = false;
                long len =0;
                long start = addr1;
                long end = addr2;
                int maxLinLen = width;

                while(!done)
                {
                    if( end - start > maxLinLen)
                    {
                        end = start+maxLinLen;
                        len = maxLinLen;
                    }
                    else
                    {
                        len = end-start;
                    }
                    sb.append(String.format("SET(0x%x,0x",start));
                    for(long i = start; i < end; i++)
                    {
                        sb.append(String.format("%x2",rom[(int) i]));
                    }
                    sb.append(")\n");
                    start=end;

                    if( end >= addr2)
                        done = true;

                    end+=maxLinLen;
                    if(end > addr2)
                        end = addr2+1;// otherwise we skip last byte
                }
                ret = sb.toString();
            }
            return ret;
        }
        

    }

