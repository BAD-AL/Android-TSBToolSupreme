package com.example.tsbtoolsupreme;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


    /// <summary>
    /// Summary description for TecmoToolFactory.
    /// </summary>
    public class TecmoToolFactory
    {

        public static ITecmoTool GetToolForRom(String fileName)
        {
            ITecmoTool tool = null;
            ROM_TYPE type = ROM_TYPE.NONE;
            try
            {
                type = CheckRomType(fileName);
            }
            catch(Exception e)
            {
                MainClass.ShowError(String.format("ERROR determining ROM type. Exception=\n%s\n%s",
                    e.getMessage(),e.getStackTrace()));
                return null;
            }

            if( type == ROM_TYPE.CXROM )
            {
                tool = new CXRomTSBTool();
                tool.Init(fileName);
                TecmoTool.Teams = new String[] 
                    {
                        "bills",     "dolphins", "patriots", "jets",
                        "bengals",    "browns",  "ravens",   "steelers",
                        "colts",      "texans",  "jaguars",  "titans",
                        "broncos",    "chiefs",  "raiders",  "chargers",  
                        "redskins",   "giants",  "eagles",   "cowboys",
                        "bears",      "lions",   "packers",  "vikings",   
                        "buccaneers", "saints",  "falcons",  "panthers",
                         
                        "AFC",     "NFC",
                        "49ers",   "rams", "seahawks",   "cardinals"
                    };
                
            }
            else if( type == ROM_TYPE.SNES )
            {
                TecmoTool.Teams = new String[] {
                "bills",   "colts",  "dolphins", "patriots",  "jets",
                "bengals", "browns", "oilers",   "steelers",
                "broncos", "chiefs", "raiders",  "chargers",  "seahawks",
                "cowboys", "giants", "eagles",   "cardinals", "redskins",
                "bears",   "lions",  "packers",  "vikings",   "buccaneers",
                "falcons", "rams",   "saints",   "49ers"
                  };
                if( fileName != null )
                    tool = new SNES_TecmoTool(fileName);
                else
                    tool = new SNES_TecmoTool();
                    
            }
            else
            {
                if( fileName != null )
                    tool = new TecmoTool(fileName);
                else
                    tool = new TecmoTool();
                TecmoTool.Teams = new String[] 
                    {
                        "bills",   "colts",  "dolphins", "patriots",  "jets",
                        "bengals", "browns", "oilers",   "steelers",
                        "broncos", "chiefs", "raiders",  "chargers",  "seahawks",
                        "redskins","giants", "eagles",   "cardinals", "cowboys",
                        "bears",   "lions",  "packers",  "vikings",   "buccaneers",
                        "49ers",   "rams",   "saints",   "falcons"
                    };
            }

            return tool;
        }

        /// <summary>
        /// returns 0 if regular NES TSB rom
        ///         1 if it's cxrom TSBROM type.
        /// Throws exceptions (UnauthorizedAccessException and others)
        /// </summary>
        /// <param name="fileName"></param>
        /// <returns></returns>
        public static ROM_TYPE CheckRomType(String fileName ) throws Exception
        {
            ROM_TYPE ret = ROM_TYPE.NES;
            
            File f1 = new File(fileName);
            FileInputStream s1 = null;
            
            try
            {
                if( f1.exists())
                {
                    byte[] fileBytes = null;
                    s1 = new FileInputStream(f1);
                    long len = f1.length();
                    fileBytes = new byte[(int) len];
                    s1.read(fileBytes, 0, (int) len);
                    
                    if( fileBytes != null && 
                        fileBytes.length > 0x99 &&
                        fileBytes[0x48] == (byte)0xff )
                        //                    if( fileName.ToLower().EndsWith(".nes") && len > 0x70000 ) //cxrom size=0x80010
                    {
                        ret = ROM_TYPE.CXROM;
                    }
                    else if( fileName.toLowerCase().endsWith(".smc"))
                    {
                        ret = ROM_TYPE.SNES;
                    }
                }
            }
            finally
            {
                if( s1 != null )
                    s1.close();
            }

            return ret;
        }
    }
    