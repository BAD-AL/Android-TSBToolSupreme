package com.example.tsbtoolsupreme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import javax.swing.JOptionPane;

/// <summary>
/// Summary description for SNES_TecmoTool.
/// Location = pointer - 0x8000 + 0x0010;
/// Where pointer is of the 'swapped' format like '0x86dd'
/// </summary>
public class SNES_TecmoTool implements ITecmoTool
{

    private byte[] outputRom;
    private final int nameNumberSegmentEnd = 0x17b7f0;
    private final int namePointersStart = 0x178038;
    private final int playerNumberNameDataStart = 0x17873a;
    private final int teamSimOffensivePrefStart = 0x15DFA; //0x27526;
    protected int dataPositionOffset = 0x170000;
    private final int pr_kr_start_offset = 0x170c90;
    private final int pr_kr_team_start_offset = 0x170cb0;
    private final int lastPointer = 0x178738; //= 0x178736;
    private final int ROM_LENGTH = 1572864;

    /// <summary>
    /// Returns the rom version 
    /// </summary>
    public String getRomVersion()
    {
        return "SNES";
    }
    //        private boolean mShowTeamFormation = false;
    //
    //        private int startScreenLine1Loc = 0xc4ec;// TODO allow user to edit these 2 lines.
    //        private int startScreenLine2Loc = 0xc504;
    private Vector<String> errors = new Vector<String>();

    public byte[] getOutputRom()
    {
        return outputRom;
    }

    public void setOutputRom(byte[] data)
    {
        outputRom = data;
    }

    public boolean getShowOffPref()
    {
        return mShowOffPref;
    }

    public void setShowOffPref(boolean pref)
    {
        mShowOffPref = pref;
    }

    public Vector<String> getErrors()
    {
        return this.errors;
    }

    ;

        public void setErrors(Vector<String> errors)
    {
        this.errors = errors;
    }
    public static boolean AUTO_CORRECT_SCHEDULE = true;
    private boolean mShowOffPref = true;
    private final int billsQB1SimLoc = 0x26acf; //0x18163;
    private final int billsRESimLoc = 0x26ae7; //0x1817b;
    private final int billsTeamSimLoc = 0x26afe;//0x18192;
    private final int teamSimOffset = 0x30;
    private final int billsQB1AbilityStart = 0x17e000;
    private final int teamAbilityOffset = 0x7D;
    public final int QUARTER_LENGTH = 0xA0EE;
    private int[] abilityOffsets =
    {
        0x00, 0x05, 0x0A, 0x0E, 0x12, 0x16, 0x1A, 0x1E, 0x22, 0x26, 0x2A,
        0x2E, 0x32, 0x35, 0x38, 0x3B, 0x3E, 0x41, 0x45, 0x49, 0x4D, 0x51,
        0x55, 0x59, 0x5D, 0x61, 0x65, 0x69, 0x6D, 0x71, 0x75, 0x79
    };
    private int[] gameYearLocations =
    {
        0x2e16b, 0x12329e, 0x123348, 0x171069, 0x1712fb,
        0x17133c, 0x172c84, 0x172d02, //0x172d10, //0x172cae, 
        /*0x172d02,*/ 0x172d3e, 0x172d5b, 0X172D1F
    };
    private String[] positionNames =
    {
        "QB1", "QB2", "RB1", "RB2", "RB3", "RB4", "WR1", "WR2", "WR3", "WR4", "TE1",
        "TE2", "C", "LG", "RG", "LT", "RT",
        "RE", "NT", "LE", "ROLB", "RILB", "LILB", "LOLB", "RCB", "LCB", "FS", "SS",
        "K", "P", "DB1", "DB2"
    };
    private static String[] teams =
    {
        "bills", "colts", "dolphins", "patriots", "jets",
        "bengals", "browns", "oilers", "steelers",
        "broncos", "chiefs", "raiders", "chargers", "seahawks",
        "cowboys", "giants", "eagles", "cardinals", "redskins",
        "bears", "lions", "packers", "vikings", "buccaneers",
        "falcons", "rams", "saints", "49ers"
    };
    private static String[] mSimTeams =
    {
        "bills", "colts", "dolphins", "patriots", "jets",
        "bengals", "browns", "oilers", "steelers",
        "broncos", "chiefs", "raiders", "chargers", "seahawks",
        "redskins", "giants", "eagles", "cardinals", "cowboys",
        "bears", "lions", "packers", "vikings", "buccaneers",
        "49ers", "rams", "saints", "falcons"
    };
    private Hashtable<Integer, Integer> abilityMap;

    public SNES_TecmoTool()
    {
    }

    public SNES_TecmoTool(String fileName)
    {
        Init(fileName);
    }

    public String[] GetTeams()
    {
        return teams;
    }

    public String[] getPositionNames(){
    	return positionNames;
    }
    
    public String[] GetPositionNames()
    {
        return positionNames;
    }

    public boolean IsValidPosition(String pos)
    {
        boolean ret = false;
        for (int i = 0; i < positionNames.length; i++)
        {
            if (pos.equals(positionNames[i]))
            {
                ret = true;
                break;
            }
        }
        return ret;
    }

    public boolean IsValidTeam(String team)
    {
        boolean ret = false;
        for (int i = 0; i < teams.length; i++)
        {
            if (team.equals(teams[i]))
            {
                ret = true;
                break;
            }
        }
        return ret;
    }

    public boolean Init(String fileName)
    {
        abilityMap = new Hashtable<Integer, Integer>();
        abilityMap.put(6, 0x00);
        abilityMap.put(13, 0x01);
        abilityMap.put(19, 0x02);
        abilityMap.put(25, 0x03);
        abilityMap.put(31, 0x04);
        abilityMap.put(38, 0x05);
        abilityMap.put(44, 0x06);
        abilityMap.put(50, 0x07);
        abilityMap.put(56, 0x08);
        abilityMap.put(63, 0x09);
        abilityMap.put(69, 0x0a);
        abilityMap.put(75, 0x0b);
        abilityMap.put(81, 0x0c);
        abilityMap.put(88, 0x0d);
        abilityMap.put(94, 0x0e);
        abilityMap.put(100, 0x0f);

        if (ReadRom(fileName))
        {
            //helper = new ScheduleHelper(outputRom);
            return true;
        }
        return false;
    }

    /**
     *
     * @param len the length of the ROM
     * @return true if it's the correct length, false otherwise.
     */
    public boolean IsValidRomSize(long len)
    {
        boolean ret = false;
        if (len == ROM_LENGTH)
        {
            ret = true;
        }
        return ret;
    }

    public boolean ReadRom(String filename)
    {
        boolean ret = false;
        try
        {
            File f1 = new File(filename);
            long len = f1.length();
            boolean wrongSize = false;

            if (len != ROM_LENGTH)
            {
                wrongSize = true;
                if (MainClass.GUI_MODE)
                {
                  //  JOptionPane.showMessageDialog(null,
                  String error=          "Warning! \n\n"
                            + "The input Rom is not the correct Size (" + ROM_LENGTH + " bytes).\n\n"
                            + "You should only continue if you know for sure that you are loading a snes TSB1 ROM.\n\n"
                            + "Do you want to continue?";//);
                  MainClass.ShowError(error);
                } else
                {
                    String msg =
                            "ERROR! ROM '" + filename + "' is not the correct length.\n"
                            + "    Legit TSB1 snes ROMS are " + ROM_LENGTH + " bytes long.\n"
                            + "    If you know this is really a snes TSB1 ROM, you can force TSBToolSupreme to load it in GUI mode.";
                    errors.add(msg);
                }
            }

            if (!wrongSize)
            {
                FileInputStream s1 = new FileInputStream(f1);
                outputRom = new byte[(int) len];
                s1.read(outputRom, 0, (int) len);
                s1.close();
                ret = true;
            }
        } catch (Exception e)
        {
            MainClass.ShowError(e.toString());
        }
        return ret;
    }

    public void SaveRom(String filename)
    {
        if (filename != null)
        {
            try
            {
                long len = outputRom.length;
                FileOutputStream s1 = new FileOutputStream(filename, false);
                s1.write(outputRom, 0, (int) len);
                s1.close();
            } catch (Exception e)
            {
                 MainClass.ShowError(e.toString());
            }
        } else
        {
            errors.add("ERROR! You passed a null filename");
        }
    }

    /// <summary>
    /// Returns a String consisting of number, name\n for all players in the game.
    /// </summary>
    /// <returns></returns>
    public String GetPlayerStuff(boolean jerseyNumber_b, boolean name_b, boolean face_b,
            boolean abilities_b, boolean simData_b) throws Exception
    {
        StringBuilder sb = new StringBuilder(16 * 28 * 30 * 3);
        String team = "";
        for (int i = 0; i < teams.length; i++)
        {
            team = teams[i];
            sb.append(String.format("TEAM=%s\n", team));
            for (int j = 0; j < positionNames.length; j++)
            {
                sb.append(GetPlayerData(team, positionNames[j], abilities_b, jerseyNumber_b, face_b, name_b, simData_b) + "\n");
            }
        }
        return sb.toString();
    }

    public String GetSchedule()
    {
        String ret = "";
        if (outputRom != null)
        {
            SNES_ScheduleHelper sh2 = new SNES_ScheduleHelper(outputRom);
            ret = sh2.GetSchedule();
            Vector<String> errors = sh2.GetErrorMessages();
            if (errors != null && errors.size() > 0)
            {
                MainClass.ShowErrors(errors);
            }
        }
        return ret;
    }

    public void SetYear(String year)
    {
        if (year == null || year.length() != 4)
        {
            errors.add(String.format("ERROR! (low level) %s is not a valid year.", year));
            return;
        }
        int location;
        for (int i = 0; i < gameYearLocations.length; i++)
        {
            location = gameYearLocations[i];
            outputRom[location] = (byte) year.charAt(0);
            outputRom[location + 1] = (byte) year.charAt(1);
            outputRom[location + 2] = (byte) year.charAt(2);
            outputRom[location + 3] = (byte) year.charAt(3);
        }
        if (!year.equals("1993"))
        {
            // This spot in the ROM doesn't like change
            // so we just blank it out.
            outputRom[0x172d02] = (byte) ' ';
            outputRom[0x172d03] = (byte) ' ';
            outputRom[0x172d04] = (byte) ' ';
            outputRom[0x172d05] = (byte) ' ';
        }
        try
        {
            //0x2E193 = 28th  (28th superbowl)
            int theYear = Integer.parseInt(year);
            int superbowlNumber = theYear - 1965;
            if (superbowlNumber < 0)
            {
                superbowlNumber = 0;
            }

            String sbw;// = superbowlNumber.toString();

            String suffix = "TH";
            int test = superbowlNumber % 10;

            switch (test)
            {
                case 1:
                    suffix = "ST";
                    break;
                case 2:
                    suffix = "ND";
                    break;
                case 3:
                    suffix = "RD";
                    break;
            }
            if (superbowlNumber < 10)
            {
                sbw = " " + superbowlNumber + suffix;
            } else if (superbowlNumber < 21)
            {
                sbw = " " + superbowlNumber + "TH";
            } else
            {
                sbw = superbowlNumber + suffix;
            }


            outputRom[0x2E193] = (byte) sbw.charAt(0);
            outputRom[0x2E194] = (byte) sbw.charAt(1);
            outputRom[0x2E195] = (byte) sbw.charAt(2);
            outputRom[0x2E196] = (byte) sbw.charAt(3);
        } catch (Exception e)
        {
            errors.add("Problem setting superbowl number.");
        }
    }

    public String GetYear()
    {
        int location = gameYearLocations[0];
        String ret = "";
        for (int i = location; i < location + 4; i++)
        {
            ret += (char) outputRom[i];
        }

        return ret;
    }

    public void InsertPlayer(String team,
            String position,
            String fname,
            String lname,
            byte number) throws Exception
    {
        if (!IsValidPosition(position) || fname == null || lname == null || fname.length() < 1 || lname.length() < 1)
        {
            errors.add(String.format("ERROR! (low level) InsertPlayer:: Player name or position invalid"));
        } else
        {
            fname = fname.toLowerCase();
            lname = lname.toUpperCase(); //15 18  char max for name
            if (lname.length() + fname.length() > 17)
            {
                errors.add(String.format("Warning!! There is a 17 character limit for names\n '%s %d' is %d characters long.",
                        fname, lname, fname.length() + lname.length()));
                if (lname.length() > 16)
                {
                    lname = lname.substring(0, 12);
                    //fname =""+fname[0]+".";
                    fname = String.format("%s.", fname.charAt(0));
                } else
                {
                    fname = String.format("%s.", fname.charAt(0));
                }
                //fname = ""+fname[0];

                errors.add(String.format("Name will be %s %s", fname, lname));
            }
            if (fname.length() < 1)
            {
                fname = "Joe";
            }
            if (lname.length() < 1)
            {
                lname = "Nobody";
            }

            String oldName = GetName(team, position);
            byte[] bytes = new byte[1 + fname.length() + lname.length()];
            int change = bytes.length - oldName.length();
            int i = 0;
            bytes[0] = number;
            for (i = 1; i < fname.length() + 1; i++)
            {
                bytes[i] = (byte) fname.charAt(i - 1);
            }
            for (int j = 0; j < lname.length(); j++)
            {
                bytes[i++] = (byte) lname.charAt(j);
            }
            int pos = GetPointerPosition(team, position);

            UpdatePlayerData(team, position, bytes, change);
            AdjustDataPointers(pos, change);
        }
    }

    private void AdjustDataPointers(int pos, int change)
    {
        byte low, hi;
        int word;
        // last pointer is at 0x178738

        int i = 0;
        int end = lastPointer + 1;
        for (i = pos + 2; i < end; i += 2)
        {
            low = outputRom[i];
            hi = outputRom[i + 1];
            word = 0xff & hi;
            word = word << 8;
            word += 0xff & low;
            word += change;
            low = (byte) (word & 0x00ff);
            word = word >> 8;
            hi = (byte) word;
            outputRom[i] = low;
            outputRom[i + 1] = hi;
        }
    }
    /// <summary>
    /// 
    /// </summary>
    /// <param name="team">The team the player is assigned to.</param>
    /// <param name="position">The player's position ('QB1', 'WR1' ...)</param>
    /// <returns></returns>

    public String GetName(String team, String position) throws Exception
    {
        if (!IsValidTeam(team) || !IsValidPosition(position))
        {
            errors.add(String.format("ERROR! (low level) GetName:: team '%s' or position '%s' is invalid.",
                    team, position));
            return null;
        }
        int pos = GetDataPosition(team, position);
        int nextPos = GetNextDataPosition(team, position);
        String name = "";

        if (pos < 0)
        {
            return "ERROR!";
        }
        if (nextPos > 0)
        {
            //start at pos+1 to skip his jersey number. 
            for (int i = pos + 1; i < nextPos; i++)
            {
                name += (char) outputRom[i];
            }
        } else
        { // 49ers DB2
            for (int i = pos + 1; outputRom[i] != (byte) 0xff; i++)
            {
                name += (char) outputRom[i];
            }
        }
        int split = 1;
        for (int i = 0; i < name.length(); i++)
        {
            if ((byte) name.charAt(i) > 64 && (byte) name.charAt(i) < 91)
            {
                split = i;
                break;
            }
        }

        String first, last, full;
        full = null;
        try
        {
            first = name.substring(0, split);
            last = name.substring(split);
            full = first + " " + last;
        } catch (Exception e)
        {
            return full;
        }
        return full;
    }

    public String GetPlayerData(String team, String position) 
    {
    	String ret = "";
    	try {
			ret = GetPlayerData(team, position, true, true, true, true, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return ret;
    }
    
    public String GetPlayerData(String team, String position, boolean ability_b,
            boolean jerseyNumber_b, boolean face_b, boolean name_b, boolean simData_b) throws Exception
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) Team %s is invalid.", team));
            return null;
        } else if (!IsValidPosition(position))
        {
            errors.add(String.format("ERROR! (low level) position %s is invalid.", position));
            return null;
        }

        StringBuilder result = new StringBuilder();

        //result.append( String.format("%s, %s, Face=0x{2:x}, ",
        //    position, GetName(team,position), GetFace(team,position)));
        result.append(String.format("%s, ", position));
        if (name_b)
        {
            result.append(String.format("%s, ", GetName(team, position)));
        }
        if (face_b)
        {
            result.append(String.format("Face=0x%x, ", GetFace(team, position)));
        }
        int location = GetDataPosition(team, position);

        if (location < 0)
        {
            return "Messed Up Pointer";
        }

        String jerseyNumber = String.format("#%x, ", (byte) outputRom[location]);
        if (jerseyNumber_b)
        {
            result.append(jerseyNumber);
        }
        if (ability_b)
        {
            result.append(GetAbilityString(team, position));
        }
        int[] simData = GetPlayerSimData(team, position);
        if (simData != null && simData_b)
        {
            result.append(String.format(",[%s]", StringifyArray(simData)));
        }
        return result.toString();
    }

    public String GetKey()
    {
        String ret = "# TEAM:\n"
                + "#  name, SimData  0x<offense><defense><offense preference>\n"
                + "#  Offensive pref values 0-3. \n"
                + "#     0 = Little more rushing, 1 = Heavy Rushing,\n"
                + "#     2 = little more passing, 3 = Heavy Passing.\n"
                + "# credit to Jstout for figuring out 'offense preference'\n"
                + "# -- Quarterbacks:\n"
                + "# Position, First name Last name, FaceID, Jersey number, RS, RP, MS, HP, PS, PC, PA, APB, [Sim rush, Sim pass, Sim Pocket].\n"
                + "# -- Offensive Skill players (non-QB):\n"
                + "# Position, First name Last name, FaceID, Jersey number, RS, RP, MS, HP, BC, REC, [Sim rush, Sim catch, Sim punt Ret, Sim kick ret].\n"
                + "# -- Offensive Linemen:\n"
                + "# Position, First name Last name, FaceID, Jersey number, RS, RP, MS, HP\n"
                + "# -- Defensive Players:\n"
                + "# Position, First name Last name, FaceID, Jersey number, RS, RP, MS, HP, PI, QU, [Sim pass rush, Sim coverage].\n"
                + "# -- Punters and Kickers:\n"
                + "# Position, First name Last name, FaceID, Jersey number, RS, RP, MS, HP, KA, AKB,[ Sim kicking ability].\n";
        return ret;
    }

    public String GetTeamPlayers(String team) throws Exception
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) GetTeamPlayers:: team %s is invalid.", team));
            return null;
        }

        StringBuilder result = new StringBuilder(41 * positionNames.length);
        String pos;
        byte teamSimData = GetTeamSimData(team);
        String data = "";
        if (teamSimData < 0xf)
        {
            data = String.format("0%x", teamSimData);
        } else
        {
            data = String.format("%x", teamSimData);
        }
        if (getShowOffPref())
        {
            data += GetTeamSimOffensePref(team);
        }

        String teamString = String.format("TEAM = %s SimData=0x%s", team, data);
        result.append(teamString);

        if (TecmoTool.ShowTeamFormation)
        {
            result.append(String.format(", %s", GetTeamOffensiveFormation(team)));
        }
        result.append("\n");

        if (TecmoTool.ShowPlaybook)
        {
            result.append(String.format("%s\n", GetPlaybook(team)));
        }

        for (int i = 0; i < positionNames.length; i++)
        {
            pos = positionNames[i];
            result.append(String.format("%s\n", GetPlayerData(team, pos, true, true, true, true, true)));
        }
        result.append(String.format("%s\n", GetReturnTeam(team)));
        result.append(String.format("KR, %s\nPR, %s\n", GetKickReturner(team), GetPuntReturner(team)));
        result.append("\n");
        return result.toString();
    }

    public String GetAll() throws Exception
    {
        String team;
        StringBuilder all = new StringBuilder(30 * 41 * positionNames.length);
        String year = String.format("YEAR=%s\n", GetYear());
        all.append(year);
        for (int i = 0; i < teams.length; i++)
        {
            team = teams[i];
            all.append(GetTeamPlayers(team));
        }
        return all.toString();
    }

    /// <summary>
    /// Gets the point in the player number name data that a player's data begins.
    /// </summary>
    /// <param name="team"></param>
    /// <param name="position"></param>
    /// <returns></returns>
    public int GetDataPosition(String team, String position) throws Exception
    {
        if (!IsValidTeam(team) || !IsValidPosition(position))
        {
            throw new Exception(
                    String.format("ERROR! (low level) GetDataPosition:: either team %s or position %s is invalid.", team, position));
        }
        int teamIndex = GetTeamIndex(team);
        int positionIndex = GetPositionIndex(position);
        // the players total index (QB1 bills=0, QB2 bills=2 ...)
        int guy = teamIndex * positionNames.length + positionIndex;
        int pointerLocation = namePointersStart + (2 * guy);
        byte lowByte = outputRom[pointerLocation];
        int hiByte = 0xff & outputRom[pointerLocation + 1];
        hiByte = hiByte << 8;
        hiByte = hiByte + (0xff & lowByte);

        //int ret = hiByte - 0x8000 + 0x010;
        int ret = hiByte + dataPositionOffset;
        return ret;
    }

    /// <summary>
    /// Get the starting point of the guy AFTER the one passed to this method.
    /// </summary>
    /// <param name="team"></param>
    /// <param name="position"></param>
    /// <returns></returns>
    public int GetNextDataPosition(String team, String position) throws Exception
    {
        if (!IsValidTeam(team) || !IsValidPosition(position))
        {
            throw new Exception(
                    String.format("ERROR! (low level) GetNextDataPosition:: either team %s or position %s is invalid.", team, position));
        }

        int ti = GetTeamIndex(team);
        int pi = GetPositionIndex(position);
        pi++;
        if (position.equals("DB2")/*"P"*/)
        {
            ti++;
            pi = 0;
        }
        //if(team == "falcons" && position == "P" )
        if (team.equals("49ers") && position.equals("DB2"))
        {
            return -1;
        } else
        {
            return GetDataPosition(teams[ti], positionNames[pi]);
        }
    }

    private int GetPointerPosition(String team, String position) throws Exception
    {
        if (!IsValidTeam(team) || !IsValidPosition(position))
        {
            throw new Exception(
                    String.format("ERROR! (low level) GetPointerPosition:: either team %s or position %s is invalid.", team, position));
        }
        int teamIndex = GetTeamIndex(team);
        int positionIndex = GetPositionIndex(position);
        int playerSpot = teamIndex * positionNames.length + positionIndex;
        //if(team == "falcons" && position == "P")
        //    return 0x6d6;
        if (team.equals("49ers") && position.equals("DB2"))
        //return lastPointer;
        {
            return lastPointer - 2;
        }

        if (positionIndex < 0)
        {
            errors.add(String.format("ERROR! (low level) Position '%s' does not exist. Valid positions are:", position));
            for (int i = 1; i <= positionNames.length; i++)
            {
                System.err.printf("%s\t", positionNames[i - 1]);
            }
            return -1;
        }
        int loc = namePointersStart + (teamIndex * 0x40) + (positionIndex * 2);
        return loc;
    }

    /// <summary>
    /// Sets the player data (jersey number, player name) in the data segment.
    /// </summary>
    /// <param name="team">The team the player is assigned to.</param>
    /// <param name="position">The position the player is assigned to.</param>
    /// <param name="bytes">The player's number and name data. </param>
    public void UpdatePlayerData(String team, String position, byte[] bytes, int change) throws Exception
    {
        if (!IsValidTeam(team) || !IsValidPosition(position))
        {
            throw new Exception(
                    String.format("ERROR! (low level) UpdatePlayerData:: either team %s or position %s is invalid.", team, position));
        }
        if (bytes == null)
        {
            return;
        }

        int dataStart = this.GetDataPosition(team, position);
        // need to do a cleaver splice here.
        ShiftDataAfter(team, position, change);
        int j = 0;
        int i;
        for (i = dataStart; j < bytes.length; i++)
        {
            outputRom[i] = bytes[j++];
        }

        if (team.equals("49ers") && position.equals("DB2"))
        {
            while (outputRom[i] != (byte) 0xff)
            {
                outputRom[i++] = (byte) 0xff;
            }
        }
    }

    private void ShiftDataAfter(String team, String position, int shiftAmount) throws Exception
    {
        if (!IsValidTeam(team) || !IsValidPosition(position))
        {
            throw new Exception(
                    String.format("ERROR! (low level) ShiftDataAfter:: either team %s or position %s is invalid.", team, position));
        }

        if (team.equals(teams[teams.length - 1]) && position.equals("DB2" /* "P"*/))
        {
            return;
        }

        //int endPosition = 0x0300F; //(end of name-number segment)

        int endPosition = nameNumberSegmentEnd;

        while (outputRom[endPosition] == (byte) 0xff)
        {
            endPosition--;
        }

        endPosition++;// it was set to falcons punter's last letter

        //int startPosition = GetDataPosition(teams[teamIndex], positionNames[positionIndex]);
        int startPosition = this.GetNextDataPosition(team, position);
        if (shiftAmount < 0)
        {
            ShiftDataUp(startPosition, endPosition, shiftAmount, outputRom);
        } else if (shiftAmount > 0)
        {
            ShiftDataDown(startPosition, endPosition, shiftAmount, outputRom);
        }
    }

    private void ShiftDataUp(int startPos, int endPos, int shiftAmount, byte[] data) throws Exception
    {
        if (startPos < 0 || endPos < 0)
        {
            throw new Exception(
                    String.format("ERROR! (low level) ShiftDataUp:: either startPos %d or endPos %d is invalid.", startPos, endPos));
        }

        // commented out code was in release 1
        //int end = endPos+shiftAmount;
        int i;
        if (shiftAmount > 0)
        {
            System.out.println("positive shift amount in ShiftDataUp");
        }

        for (i = startPos /*+ shiftAmount*/; i <= endPos /*end*/; i++)
        {
            data[i + shiftAmount] = data[i];
        }
        /*i--;
         for(int j=shiftAmount; j < 0; j++) 
         data[i++] = 0xff; */

        i += shiftAmount;
        while (outputRom[i] != (byte) 0xff && i < nameNumberSegmentEnd /*0x300f*/)
        {
            outputRom[i] = (byte) 0xff;
            i++;
        }

    }

    private void ShiftDataDown(int startPos, int endPos, int shiftAmount, byte[] data) throws Exception
    {
        if (startPos < 0 || endPos < 0)
        {
            throw new Exception(
                    String.format("ERROR! (low level) ShiftDataUp:: either startPos %d or endPos %d is invalid.", startPos, endPos));
        }

        for (int i = endPos + shiftAmount; i > startPos; i--)
        {
            data[i] = data[i - shiftAmount];
        }
    }

    public static int GetTeamIndex(String teamName)
    {
        int ret = -1;
        if (teamName.toLowerCase().equals("null"))
        {
            return 255;
        }
        for (int i = 0; i < teams.length; i++)
        {
            if (teams[i].equals(teamName))
            {
                ret = i;
                break;
            }
        }
        return ret;
    }

    public static int GetSimTeamIndex(String teamName)
    {
        int ret = -1;
        if (teamName.toLowerCase().equals("null"))
        {
            return 255;
        }
        for (int i = 0; i < mSimTeams.length; i++)
        {
            if (mSimTeams[i].equals(teamName))
            {
                ret = i;
                break;
            }
        }
        return ret;
    }

    /// <summary>
    /// Returns the team specified by the index passed. (0= bills).
    /// </summary>
    /// <param name="index"></param>
    /// <returns>team name on success, null on failure</returns>
    public static String GetTeamFromIndex(int index)
    {
        if (index == 255)
        {
            return "null";
        }
        if (index < 0 || index > teams.length - 1)
        {
            return null;
        }
        return teams[index];
    }

    /// <summary>
    /// 
    /// </summary>
    /// <param name="positionName"> like 'QB1', 'K','P' ... </param>
    /// <returns></returns>
    int GetPositionIndex(String positionName)
    {
        int ret = -1;
        for (int i = 0; i < positionNames.length; i++)
        {
            if (positionNames[i].equals(positionName))
            {
                ret = i;
                break;
            }
        }
        return ret;
    }

    /// <summary>
    /// 
    /// </summary>
    /// <param name="team"></param>
    /// <param name="qb">Either 'QB1' or 'QB2'</param>
    public void SetQBAbilities(String team,
            String qb,
            int runningSpeed,
            int rushingPower,
            int maxSpeed,
            int hittingPower,
            int passingSpeed,
            int passControl,
            int accuracy,
            int avoidPassBlock)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) team %s is invalid", team));
            return;
        }
        if (!qb.equals("QB1") && !qb.equals("QB2"))
        {
            errors.add(String.format("ERROR! (low level) Cannot set qb ablities for %s", qb));
            return;
        }
        runningSpeed = GetAbility(runningSpeed);
        rushingPower = GetAbility(rushingPower);
        maxSpeed = GetAbility(maxSpeed);
        hittingPower = GetAbility(hittingPower);
        passingSpeed = GetAbility(passingSpeed);
        passControl = GetAbility(passControl);
        accuracy = GetAbility(accuracy);
        avoidPassBlock = GetAbility(avoidPassBlock);

        if (!IsValidAbility(runningSpeed)
                || !IsValidAbility(rushingPower)
                || !IsValidAbility(maxSpeed)
                || !IsValidAbility(hittingPower)
                || !IsValidAbility(passingSpeed)
                || !IsValidAbility(passControl)
                || !IsValidAbility(accuracy)
                || !IsValidAbility(avoidPassBlock))
        {
            errors.add(String.format("ERROR! (low level) Abilities for %s on %s were not set.", qb, team));
            PrintValidAbilities();
            return;
        }
        SaveAbilities(team, qb, runningSpeed, rushingPower, maxSpeed, hittingPower, passingSpeed, passControl);
        int teamIndex = GetTeamIndex(team);
        int posIndex = GetPositionIndex(qb);
        int location = (teamIndex * teamAbilityOffset) + abilityOffsets[posIndex] + billsQB1AbilityStart;
        int lastByte = accuracy << 4;
        lastByte += avoidPassBlock;
        outputRom[location + 4] = (byte) lastByte;
        lastByte = passingSpeed << 4;
        lastByte += passControl;
        outputRom[location + 3] = (byte) lastByte;
    }

    public void SetSkillPlayerAbilities(String team, String pos,
            int runningSpeed, int rushingPower, int maxSpeed, int hittingPower,
            int ballControl, int receptions)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) team %s is invalid",
                    team));
            return;
        }

        if (!pos.equals("RB1") && !pos.equals("RB2") && !pos.equals("RB3") && !pos.equals("RB4")
                && !pos.equals("WR1") && !pos.equals("WR2") && !pos.equals("WR3") && !pos.equals("WR4")
                && !pos.equals("TE1") && !pos.equals("TE2"))
        {
            errors.add(String.format("ERROR! (low level) Cannot set skill player ablities for %s.", pos));
            return;
        }
        runningSpeed = GetAbility(runningSpeed);
        rushingPower = GetAbility(rushingPower);
        maxSpeed = GetAbility(maxSpeed);
        hittingPower = GetAbility(hittingPower);
        ballControl = GetAbility(ballControl);
        receptions = GetAbility(receptions);

        if (!IsValidAbility(runningSpeed) || !IsValidAbility(rushingPower)
                || !IsValidAbility(maxSpeed) || !IsValidAbility(hittingPower)
                || !IsValidAbility(receptions) || !IsValidAbility(ballControl))
        {
            errors.add(String
                    .format("ERROR! (low level) Invalid attribute. Abilities for %s on %s were not set.",
                    pos, team));
            PrintValidAbilities();
            return;
        }
        SaveAbilities(team, pos, runningSpeed, rushingPower, maxSpeed,
                hittingPower, ballControl, receptions);
    }

    public void SetKickPlayerAbilities(String team, String pos,
            int runningSpeed, int rushingPower, int maxSpeed, int hittingPower,
            int kickingAbility, int avoidKickBlock)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) team %s is invalid",
                    team));
            return;
        }

        if (!pos.equals("K") && !pos.equals("P"))
        {
            errors.add(String.format("Cannot set kick player ablities for %s.",
                    pos));
            return;
        }
        runningSpeed = GetAbility(runningSpeed);
        rushingPower = GetAbility(rushingPower);
        maxSpeed = GetAbility(maxSpeed);
        hittingPower = GetAbility(hittingPower);
        kickingAbility = GetAbility(kickingAbility);
        avoidKickBlock = GetAbility(avoidKickBlock);

        if (!IsValidAbility(runningSpeed) || !IsValidAbility(rushingPower)
                || !IsValidAbility(maxSpeed) || !IsValidAbility(hittingPower)
                || !IsValidAbility(kickingAbility)
                || !IsValidAbility(avoidKickBlock))
        {
            errors.add(String.format("Abilities for %s on %s were not set.",
                    pos, team));
            PrintValidAbilities();
            return;
        }
        SaveAbilities(team, pos, runningSpeed, rushingPower, maxSpeed,
                hittingPower, kickingAbility, avoidKickBlock);
    }

    public void SetDefensivePlayerAbilities(String team, String pos,
            int runningSpeed, int rushingPower, int maxSpeed, int hittingPower,
            int passRush, int interceptions)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) team %s is invalid",
                    team));
            return;
        }

        if (!pos.equals("RE") && !pos.equals("NT") && !pos.equals("LE") && !pos.equals("ROLB")
                && !pos.equals("RILB") && !pos.equals("LILB") && !pos.equals("LOLB")
                && !pos.equals("RCB") && !pos.equals("LCB") && !pos.equals("SS") && !pos.equals("FS")
                && !pos.equals("DB1") && !pos.equals("DB2"))
        {
            errors.add(String.format("Cannot set defensive player ablities for %s.", pos));
            return;
        }
        runningSpeed = GetAbility(runningSpeed);
        rushingPower = GetAbility(rushingPower);
        maxSpeed = GetAbility(maxSpeed);
        hittingPower = GetAbility(hittingPower);
        passRush = GetAbility(passRush);
        interceptions = GetAbility(interceptions);

        if (!IsValidAbility(runningSpeed) || !IsValidAbility(rushingPower)
                || !IsValidAbility(maxSpeed) || !IsValidAbility(hittingPower)
                || !IsValidAbility(passRush) || !IsValidAbility(interceptions))
        {
            errors.add(String.format("Abilities for %s on %s were not set.",
                    pos, team));
            PrintValidAbilities();
            return;
        }
        SaveAbilities(team, pos, runningSpeed, rushingPower, maxSpeed,
                hittingPower, passRush, interceptions);
    }

    public void SetOLPlayerAbilities(String team, String pos, int runningSpeed,
            int rushingPower, int maxSpeed, int hittingPower)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) team %s is invalid",
                    team));
            return;
        }

        if (!pos.equals("C") && !pos.equals("RG") && !pos.equals("LG") && !pos.equals("RT")
                && !pos.equals("LT"))
        {
            errors.add(String.format("Cannot set OL player ablities for %s.",
                    pos));
            return;
        }
        runningSpeed = GetAbility(runningSpeed);
        rushingPower = GetAbility(rushingPower);
        maxSpeed = GetAbility(maxSpeed);
        hittingPower = GetAbility(hittingPower);

        if (!IsValidAbility(runningSpeed) || !IsValidAbility(rushingPower)
                || !IsValidAbility(maxSpeed) || !IsValidAbility(hittingPower))
        {
            errors.add(String.format("Abilities for %s on %s were not set.",
                    pos, team));
            PrintValidAbilities();
            return;
        }// GetAbility
        SaveAbilities(team, pos, runningSpeed, rushingPower, maxSpeed,
                hittingPower, -1, -1);
    }

    private void SaveAbilities(String team, String pos,
            int runningSpeed,
            int rushingPower,
            int maxSpeed,
            int hittingPower,
            int bc,
            int rec)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) SaveAbilities:: team %s is invalid", team));
            return;
        } else if (!IsValidPosition(pos))
        {
            errors.add(String.format("ERROR! (low level) SaveAbilities:: position %s is invalid", pos));
            return;
        }

        int byte1, byte2, byte3;
        byte1 = (byte) rushingPower;
        byte1 = byte1 << 4;
        byte1 += (byte) runningSpeed;
        byte2 = (byte) maxSpeed;
        byte2 = byte2 << 4;
        byte2 += (byte) hittingPower;
        byte3 = (byte) bc;
        byte3 = byte3 << 4;
        byte3 += (byte) rec;
        // save data here in rom 
        int teamIndex = GetTeamIndex(team);
        int posIndex = GetPositionIndex(pos);
        int location = (teamIndex * teamAbilityOffset) + abilityOffsets[posIndex] + billsQB1AbilityStart;
        outputRom[location] = (byte) byte1;
        outputRom[location + 1] = (byte) byte2;

        if (bc > -1 && rec > -1)
        {
            outputRom[location + 3] = (byte) byte3;
        }
    }

    boolean IsValidAbility(int ab)
    {
        if (abilityMap.containsValue(ab))
        {
            return true;
        } else
        {
            return false;
        }
    }

    private byte GetAbility(int ab)
    {
        byte ret = 0;
        switch (ab)
        {
            case 6:
                ret = 0x00;
                break;
            case 13:
                ret = 0x01;
                break;
            case 19:
                ret = 0x02;
                break;
            case 25:
                ret = 0x03;
                break;
            case 31:
                ret = 0x04;
                break;
            case 38:
                ret = 0x05;
                break;
            case 44:
                ret = 0x06;
                break;
            case 50:
                ret = 0x07;
                break;
            case 56:
                ret = 0x08;
                break;
            case 63:
                ret = 0x09;
                break;
            case 69:
                ret = 0x0a;
                break;
            case 75:
                ret = 0x0b;
                break;
            case 81:
                ret = 0x0c;
                break;
            case 88:
                ret = 0x0d;
                break;
            case 94:
                ret = 0x0e;
                break;
            case 100:
                ret = 0x0f;
                break;
        }
        return ret;
    }

    private byte MapAbality(int ab)
    {
        /*if(abilityMap.ContainsKey(ab))
         return (byte) abilityMap[ab];
         else
         return 0;*/

        byte ret = 0;
        switch (ab)
        {
            case 0x00:
                ret = 6;
                break;
            case 0x01:
                ret = 13;
                break;
            case 0x02:
                ret = 19;
                break;
            case 0x03:
                ret = 25;
                break;
            case 0x04:
                ret = 31;
                break;
            case 0x05:
                ret = 38;
                break;
            case 0x06:
                ret = 44;
                break;
            case 0x07:
                ret = 50;
                break;
            case 0x08:
                ret = 56;
                break;
            case 0x09:
                ret = 63;
                break;
            case 0x0A:
                ret = 69;
                break;
            case 0x0B:
                ret = 75;
                break;
            case 0x0C:
                ret = 81;
                break;
            case 0x0D:
                ret = 88;
                break;
            case 0x0E:
                ret = 94;
                break;
            case 0x0F:
                ret = 100;
                break;
        }
        return ret;
    }

    /// <summary>
    /// Returns an array of ints mapping to a player's abilities.
    /// Like { 13, 13, 50, 56, 31, 25}. The length of the array returned varies depending
    /// on position.
    /// </summary>
    /// <param name="team">Team name like 'oilers'.</param>
    /// <param name="position">Position name like 'RB4'.</param>
    /// <returns>an array of ints.</returns>
    public int[] GetAbilities(String team, String position)
    {
        if (!IsValidTeam(team) || !IsValidPosition(position))
        {
            return null;
        }

        int[] ret =
        {
            0
        }; // ret is re-created later.
        int teamIndex = GetTeamIndex(team);
        int posIndex = GetPositionIndex(position);
        int location = (teamIndex * teamAbilityOffset) + abilityOffsets[posIndex] + billsQB1AbilityStart;
        // wild1 and wild2 map to [receptions and ball control], [pass interceptions and quickness],
        // [kicking ability and avoid kick block]
        int runningSpeed, rushingPower, maxSpeed, hittingPower, wild1, wild2, accuracy, avoidPassBlock;
        int b1, b2, b3, b4; // note 3rd byte maps to the player's face
        b1 = 0xff & outputRom[location];
        b2 = 0xff & outputRom[location + 1];
        b3 = 0xff & outputRom[location + 3];
        b4 = 0xff & outputRom[location + 4]; // this is only used for qb, but
        // since we are not assigning it
        // here,
        // it doesn't hurt to get it.
        runningSpeed = b1 & 0x0F;
        runningSpeed = MapAbality(runningSpeed);
        rushingPower = b1 & 0xF0;
        rushingPower = MapAbality(rushingPower >> 4);
        maxSpeed = b2 & 0xF0;
        maxSpeed = MapAbality(maxSpeed >> 4);
        hittingPower = b2 & 0x0F;
        hittingPower = MapAbality(hittingPower);
        wild1 = b3 & 0xF0;
        wild1 = MapAbality(wild1 >> 4);
        wild2 = b3 & 0x0F;
        wild2 = MapAbality(wild2);
        accuracy = b4 & 0xF0;
        accuracy = MapAbality(accuracy >> 4);
        avoidPassBlock = b4 & 0x0F;
        avoidPassBlock = MapAbality(avoidPassBlock);
        // switch(position)
        // {
        if (position.equals("C") || position.equals("RG") || position.equals("LG")
                || position.equals("RT") || position.equals("LT"))
        {
            ret = new int[4];
        } else if (position.equals("QB1") || position.equals("QB2"))
        {
            ret = new int[8];
            ret[4] = wild1;
            ret[5] = wild2;
            ret[6] = accuracy;
            ret[7] = avoidPassBlock;
        } else
        {
            ret = new int[6];
            ret[4] = wild1;
            ret[5] = wild2;
        }
        ret[0] = runningSpeed;
        ret[1] = rushingPower;
        ret[2] = maxSpeed;
        ret[3] = hittingPower;
        return ret;
    }

    /// <summary>
    /// Returns a String consisting of numbers, spaces and commas.
    /// Like "31, 69, 13, 13, 31, 44"
    /// </summary>
    /// <param name="team"></param>
    /// <param name="position"></param>
    /// <returns></returns>
    public String GetAbilityString(String team, String position)
    {
        if (!IsValidTeam(team) || !IsValidPosition(position))
        {
            return null;
        }
        int[] abilities = GetAbilities(team, position);
        StringBuilder stuff = new StringBuilder();

        for (int i = 0; i < abilities.length; i++)
        {
            stuff.append(abilities[i]);
            stuff.append(", ");
        }
        int start = stuff.length() - 2;
        stuff.delete(start, start + 1);// trim off last comma
        // System.out.ptintLine(stuff);
        return stuff.toString();
    }

    /// <summary>
    /// Returns the simulation data for the given team.
    /// Simulation data is of the form '0xNN' where N is a number 1-F (hex).
    /// A team's sim data of '0x57' signifies that the team has a simulation figure of
    /// '5' for offense, and '7' for defense.
    /// </summary>
    /// <param name="team">The team of interest</param>
    /// <returns></returns>
    public byte GetTeamSimData(String team)
    {
        int teamIndex = GetSimTeamIndex(team);
        if (teamIndex >= 0)
        {
            int location = teamIndex * teamSimOffset + billsTeamSimLoc;
            return outputRom[location];
        }
        return 0x00;
    }

    /// <summary>
    /// Sets the given team's offense and defense sim values.
    /// Simulation data is of the form '0xNN' where N is a number 1-F (hex).
    /// A team's sim data of '0x57' signifies that the team has a simulation figure of
    /// '5' for offense, and '7' for defense.
    /// </summary>
    /// <param name="team">The team to set.</param>
    /// <param name="values">The value to set it to.</param>
    public void SetTeamSimData(String team, byte values)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) SetTeamSimData:: team %s is invalid ", team));
            return;
        }

        int teamIndex = GetSimTeamIndex(team);
        int location = teamIndex * teamSimOffset + billsTeamSimLoc;
        //int currentValue = 0xff & outputRom[location];
        outputRom[location] = values;
        //currentValue = outputRom[location];
    }

    /// <summary>
    /// Sets the team sim offense tendency . 
    /// 00 = Little more rushing, 01 = Heavy Rushing, 
    /// 02 = little more passing, 03 = Heavy Passing. 
    /// </summary>
    /// <param name="team">the team name</param>
    /// <param name="val">the number to set it to.</param>
    /// <returns>true if set, fales if could not set it.</returns>
    public boolean SetTeamSimOffensePref(String team, int val)
    {
        int teamIndex = GetTeamIndex(team);
        if (val > -1 && val < 4 && teamIndex != -1)
        {
            int loc = teamSimOffensivePrefStart + teamIndex;
            outputRom[loc] = (byte) val;
        } else
        {
            if (teamIndex != -1)
            {
                errors.add(String.format("Can't set offensive pref to '%d' valid values are 0-3.\n", val));
            } else
            {
                errors.add(String.format("Team '%s' is invalid\n", team));
            }
        }
        return true;
    }

    /// <summary>
    /// Sets the team sim offense tendency . 
    /// 00 = Little more rushing, 01 = Heavy Rushing, 
    /// 02 = little more passing, 03 = Heavy Passing. 
    /// </summary>
    /// <param name="team">Teh team name.</param>
    /// <returns>their sim offense pref (0 - 3)</returns>
    public int GetTeamSimOffensePref(String team)
    {
        int teamIndex = GetTeamIndex(team);
        int val = -1;
        if (teamIndex > -1)
        {
            int loc = teamSimOffensivePrefStart + teamIndex;
            val = 0xff & outputRom[loc];
        } else
        {
            errors.add(String.format("Team '%s' is invalid\n", team));
        }
        return val;
    }
    private final String m2RB_2WR_1TE = "2RB_2WR_1TE";
    private final String m1RB_3WR_1TE = "1RB_3WR_1TE";
    private final String m1RB_4WR = "1RB_4WR";
    private int mTeamFormationsStartingLoc = 0xedf3;

    /// <summary>
    /// Sets the team's offensive formation.
    /// </summary>
    /// <param name="team"></param>
    /// <param name="formation"></param>
    public void SetTeamOffensiveFormation(String team, String formation)
    {
        int teamIndex = GetTeamIndex(team);
        if (teamIndex > -1 && teamIndex < 255)
        {
            int location = mTeamFormationsStartingLoc + teamIndex;
            //switch( formation )
            {
                if (formation.equals(m2RB_2WR_1TE))
                {
                    outputRom[location] = (byte) 0x00;
                } else if (formation.equals(m1RB_3WR_1TE))
                {
                    outputRom[location] = (byte) 0x01;
                } else if (formation.equals(m1RB_4WR))
                {
                    outputRom[location] = (byte) 0x02;
                } else
                {
                    errors.add(String.format(
                            "ERROR! Formation %s for team %s is invalid.", formation, team));
                    errors.add(String.format("  Valid formations are:\n  %s\n  %s\n  %s",
                            m2RB_2WR_1TE, m1RB_3WR_1TE, m1RB_4WR));
                }
            }
        } else
        {
            errors.add(String.format("ERROR! Team '%s' is invalid, Offensive Formation not set", team));
        }
    }

    /// <summary>
    /// Gets the team's offensive formation.
    /// </summary>
    /// <param name="team"></param>
    /// <returns></returns>
    public String GetTeamOffensiveFormation(String team)
    {
        int teamIndex = GetTeamIndex(team);
        String ret = "OFFENSIVE_FORMATION = ";
        if (teamIndex > -1 && teamIndex < 255)
        {
            int location = mTeamFormationsStartingLoc + teamIndex;
            int formation = 0xff & outputRom[location];

            switch (formation)
            {
                case 0x00:
                    ret += m2RB_2WR_1TE;
                    break;
                case 0x01:
                    ret += m1RB_3WR_1TE;
                    break;
                case 0x02:
                    ret += m1RB_4WR;
                    break;
                default:
                    errors.add(String.format(
                            "ERROR! Formation %s for team %s is invalid, ROM is messed up.", formation, team));
                    ret = "";
                    break;
            }
        } else
        {
            ret = "";
            errors.add(String.format("ERROR! Team '%s' is invalid, Offensive Formation get failed.", team));
        }
        return ret;
    }
    private final int mPlaybookStartLoc = 0x170d30;
    /// <summary>
    /// Returns a String like "PLAYBOOK R1, R4, R6, R8, P1, P3, P7, P3"
    /// </summary>
    /// <param name="team"></param>
    /// <returns></returns>

    public String GetPlaybook(String team)
    {
        String ret = "";
        int rSlot1, rSlot2, rSlot3, rSlot4,
                pSlot1, pSlot2, pSlot3, pSlot4;

        int teamIndex = Index(teams, team);
        if (teamIndex > -1)
        {
            int pbLocation = mPlaybookStartLoc + (teamIndex * 4);
            rSlot1 = (0xff & outputRom[pbLocation]) >> 4;
            rSlot2 = (0xff & outputRom[pbLocation]) & 0x0f;
            rSlot3 = (0xff & outputRom[pbLocation + 1]) >> 4;
            rSlot4 = (0xff & outputRom[pbLocation + 1]) & 0x0f;

            pSlot1 = (0xff & outputRom[pbLocation + 2]) >> 4;
            pSlot2 = (0xff & outputRom[pbLocation + 2]) & 0x0f;
            pSlot3 = (0xff & outputRom[pbLocation + 3]) >> 4;
            pSlot4 = (0xff & outputRom[pbLocation + 3]) & 0x0f;

            ret = String.format(
                    "PLAYBOOK R%d%d%d%d, P%d%d%d%d ",
                    rSlot1 + 1, rSlot2 + 1, rSlot3 + 1, rSlot4 + 1,
                    pSlot1 + 1, pSlot2 + 1, pSlot3 + 1, pSlot4 + 1);
        }

        return ret;
    }
    Pattern runRegex, passRegex;

    /// <summary>
    /// Sets the team's playbook
    /// </summary>
    /// <param name="runPlays">String like "R1234"</param>
    /// <param name="passPlays">String like "P4567"</param>
    public void SetPlaybook(String team, String runPlays, String passPlays)
    {
        if (runRegex == null || passRegex == null)
        {
            runRegex = Pattern.compile("R([1-8])([1-8])([1-8])([1-8])");
            passRegex = Pattern.compile("P([1-8])([1-8])([1-8])([1-8])");
        }
        Matcher runs = runRegex.matcher(runPlays);
        Matcher pass = passRegex.matcher(passPlays);

        int r1, r2, r3, r4, p1, p2, p3, p4;

        int teamIndex = Index(teams, team);
        if (teamIndex > -1 && runs.find() && pass.find())
        {
            int pbLocation = mPlaybookStartLoc + (teamIndex * 4);
            r1 = Integer.parseInt(runs.group(1).toString()) - 1;
            r2 = Integer.parseInt(runs.group(2).toString()) - 1;
            r3 = Integer.parseInt(runs.group(3).toString()) - 1;
            r4 = Integer.parseInt(runs.group(4).toString()) - 1;

            p1 = Integer.parseInt(pass.group(1).toString()) - 1;
            p2 = Integer.parseInt(pass.group(2).toString()) - 1;
            p3 = Integer.parseInt(pass.group(3).toString()) - 1;
            p4 = Integer.parseInt(pass.group(4).toString()) - 1;

            r1 = (r1 << 4) + r2;
            r3 = (r3 << 4) + r4;
            p1 = (p1 << 4) + p2;
            p3 = (p3 << 4) + p4;
            outputRom[pbLocation] = (byte) r1;
            outputRom[pbLocation + 1] = (byte) r3;
            outputRom[pbLocation + 2] = (byte) p1;
            outputRom[pbLocation + 3] = (byte) p3;
        } else
        {
            if (teamIndex < 0)
            {
                errors.add(String.format("ERROR! SetPlaybook: Team %s is Invalid.", team));
            }
            if (!runs.matches())
            {
                errors.add(String.format("ERROR! SetPlaybook Run play definition '%s 'is Invalid", runPlays));
            }
            if (!pass.matches())
            {
                errors.add(String.format("ERROR! SetPlaybook Pass play definition '%s 'is Invalid", passPlays));
            }
        }
    }
    public final int JUICE_LOCATION = 0x2699a;
    private byte[] m_JuiceArray =
    {
        0, 1, 0, 0, 0,
        1, 2, 1, 1, 1,
        1, 2, 1, 2, 2,
        1, 2, 1, 3, 2,
        2, 2, 2, 3, 3,
        2, 2, 2, 4, 3,
        2, 2, 2, 4, 4,
        2, 2, 2, 5, 4,
        2, 2, 3, 5, 5,
        2, 2, 3, 6, 5,
        2, 2, 4, 6, 6,
        3, 2, 4, 7, 6,
        3, 3, 4, 7, 7,
        3, 3, 5, 8, 7,
        3, 3, 5, 8, 8,
        3, 3, 5, 9, 8,
        3, 4, 6, 9, 9
    };

    public boolean ApplyJuice(int week, int amt)
    {
        boolean ret = true;
        if (week > 17 || week < 0 || amt > 17 || amt < 0)
        {
            ret = false;
        } else
        {
            int rom_location = JUICE_LOCATION + (week * 5);
            int index = (amt - 1) * 5;
            for (int i = 0; i < 5; i++)
            {
                outputRom[rom_location + i] = m_JuiceArray[index + i];
            }
        }
        return ret;
    }

    public int[] GetPlayerSimData(String team, String pos)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format(
                    "ERROR! (low level) GetPlayerSimData:: Invalid team %s",
                    team));
            return null;
        } else if (!IsValidPosition(pos))
        {
            errors.add(String
                    .format("ERROR! (low level) GetPlayerSimData:: Invalid Position %s",
                    pos));
            return null;
        }

        if (pos.equals("QB1") || pos.equals("QB2"))
        {
            return GetQBSimData(team, pos);
        } else if (pos.equals("RB1") || pos.equals("RB2") || pos.equals("RB3") || pos.equals("RB4")
                || pos.equals("WR1") || pos.equals("WR2") || pos.equals("WR3") || pos.equals("WR4")
                || pos.equals("TE1") || pos.equals("TE2"))
        {
            return GetSkillSimData(team, pos);
        } else if (pos.equals("RE") || pos.equals("NT") || pos.equals("LE") || pos.equals("LOLB")
                || pos.equals("LILB") || pos.equals("RILB") || pos.equals("ROLB")
                || pos.equals("RCB") || pos.equals("LCB") || pos.equals("FS") || pos.equals("SS"))
        {
            return GetDefensiveSimData(team, pos);
        } else if (pos.equals("K"))
        {
            return GetKickingSimData(team);
        } else if (pos.equals("P"))
        {
            return GetPuntingSimData(team);
        } else
        {
            return null;
        }
    }

    private int[] GetKickingSimData(String team)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) GetKickingSimData:: Invalid team %s", team));
            return null;
        }
        int[] ret = new int[1];
        int teamIndex = GetSimTeamIndex(team);
        //QB1 + 0x2E
        int location = teamIndex * teamSimOffset + billsQB1SimLoc + 0x2E;
        ret[0] = (0xff & outputRom[location]) >> 4;
        return ret;
    }

    public void SetKickingSimData(String team, int data)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) SetKickingSimData:: Invalid team %s", team));
            return;
        }
        //int teamIndex = GetTeamIndex(team);
        int teamIndex = GetSimTeamIndex(team);
        //QB1 + 0x2E
        int location = teamIndex * teamSimOffset + billsQB1SimLoc + 0x2E;
        int g = 0xff & outputRom[location];
        g = g & 0x0F;
        int g2 = data << 4;
        g = g + g2;
        outputRom[location] = (byte) g;
    }

    private int[] GetPuntingSimData(String team)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) GetPuntingSimData:: Invalid team %s", team));
            return null;
        }
        int[] ret = new int[1];
        int teamIndex = GetSimTeamIndex(team);
        //QB1 + 0x2E
        int location = teamIndex * teamSimOffset + billsQB1SimLoc + 0x2E;
        ret[0] = outputRom[location] & 0x0F;
        return ret;
    }

    public void SetPuntingSimData(String team, int data)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) SetPuntingSimData:: Invalid team %s", team));
            return;
        }
        //int teamIndex = GetTeamIndex(team);
        int teamIndex = GetSimTeamIndex(team);
        //QB1 + 0x2E
        int location = teamIndex * teamSimOffset + billsQB1SimLoc + 0x2E;
        int d = 0xff & outputRom[location];
        d = d & 0xF0;
        d += data;
        outputRom[location] = (byte) d;
    }

    private int[] GetDefensiveSimData(String team, String pos)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) GetDefensiveSimData:: Invalid team %s", team));
            return null;
        } else if (!IsValidPosition(pos))
        {
            errors.add(String.format("ERROR! (low level) GetDefensiveSimData:: Invalid Position %s", pos));
            return null;
        }

        int[] ret = new int[2];
        int teamIndex = GetSimTeamIndex(team);
        int positionIndex = GetPositionIndex(pos);
        //int location = teamIndex*teamSimOffset + (positionIndex*2) +billsQB1SimLoc - 0x0A; // OL-men have no sim data, 2*5=0xA
        int location = teamIndex * teamSimOffset + (positionIndex - 17) + billsRESimLoc;
        ret[0] = 0xff & outputRom[location]; //pass rush
        ret[1] = 0xff & outputRom[location + 0xB];// interception ability
        return ret;
    }

    /// <summary>
    /// Sets the simulation data for a defensive player.
    /// </summary>
    /// <param name="team">The team the player belongs to.</param>
    /// <param name="pos">the position he plays.</param>
    /// <param name="data">the data to set it to (length = 2).</param>
    public void SetDefensiveSimData(String team, String pos, int[] data)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) SetDefensiveSimData:: Invalid team %s", team));
            return;
        } else if (!IsValidPosition(pos))
        {
            errors.add(String.format("ERROR! (low level) SetDefensiveSimData:: Invalid Position %s", pos));
            return;
        } else if (data == null || data.length < 2)
        {
            errors.add(String.format("Error setting sim data for %s, %s. Sim data not set.", team, pos));
            return;
        }
        int teamIndex = GetSimTeamIndex(team);
        int positionIndex = GetPositionIndex(pos);
        //int location = teamIndex*teamSimOffset + (positionIndex*2) +billsLESimLoc - 0x0A; // OL-men have no sim data, 2*5=0xA
        int location = teamIndex * teamSimOffset + (positionIndex - 17) + billsRESimLoc;
        byte byte1, byte2;
        byte1 = (byte) data[0];
        byte2 = (byte) data[1];

        outputRom[location] = byte1; //pass rush
        outputRom[location + 0xB] = byte2;// interception ability
    }

    private int[] GetSkillSimData(String team, String pos)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) GetSkillSimData:: Invalid team %s", team));
            return null;
        } else if (!IsValidPosition(pos))
        {
            errors.add(String.format("ERROR! (low level) GetSkillSimData:: Invalid Position %s", pos));
            return null;
        }

        int[] ret = new int[4];
        int teamIndex = GetSimTeamIndex(team);
        int positionIndex = GetPositionIndex(pos);
        int location = teamIndex * teamSimOffset + (positionIndex * 2) + billsQB1SimLoc;
        ret[0] = (0xff & outputRom[location]) >> 4;
        ret[1] = (0xff & outputRom[location]) & 0x0F;
        ret[2] = (0xff & outputRom[location + 1]) >> 4;
        ret[3] = (0xff & outputRom[location + 1]) & 0x0F;
        return ret;
    }

    public void SetSkillSimData(String team, String pos, int[] data)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) SetSkillSimData:: Invalid team %s", team));
            return;
        } else if (!IsValidPosition(pos))
        {
            errors.add(String.format("ERROR! (low level) SetSkillSimData:: Invalid Position %s", pos));
            return;
        } else if (data == null || data.length < 4)
        {
            errors.add(String.format("Error setting sim data for %s, %s. Sim data not set.", team, pos));
            return;
        }

        int teamIndex = GetSimTeamIndex(team);
        int positionIndex = GetPositionIndex(pos);
        int location = teamIndex * teamSimOffset + (positionIndex * 2) + billsQB1SimLoc;
        int byte1, byte2;
        byte1 = data[0] << 4;
        byte1 = byte1 + data[1];
        byte2 = data[2] << 4;
        byte2 += data[3];
        outputRom[location] = (byte) byte1;
        outputRom[location + 1] = (byte) byte2;
    }

    private int[] GetQBSimData(String team, String pos)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) GetQBSimData:: Invalid team %s", team));
            return null;
        } else if (!IsValidPosition(pos))
        {
            errors.add(String.format("ERROR! (low level) GetQBSimData:: Invalid Position %s", pos));
            return null;
        }

        int[] ret = new int[3];
        int teamIndex = GetSimTeamIndex(team);

        int location = teamIndex * teamSimOffset + billsQB1SimLoc;
        if (pos.equals("QB2"))
        {
            location += 2;
        }
        ret[0] = (0xff & outputRom[location]) >> 4;
        ret[1] = outputRom[location] & 0x0F;
        ret[2] = 0xff & outputRom[location + 1];
        return ret;
    }

    public void SetQBSimData(String team, String pos, int[] data)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) SetQBSimData:: Invalid team %s", team));
            return;
        } else if (!IsValidPosition(pos))
        {
            errors.add(String.format("ERROR! (low level) SetQBSimData:: Invalid Position %s", pos));
            return;
        } else if (data == null || data.length < 2)
        {
            errors.add(String.format("Error setting sim data for %s, %s. Sim data not set.", team, pos));
            return;
        }

        int teamIndex = GetSimTeamIndex(team);

        int location = teamIndex * teamSimOffset + billsQB1SimLoc;
        if (pos.equals("QB2"))
        {
            location += 2;
        }
        int byte1, byte2;
        byte1 = (byte) data[0] << 4;
        byte1 = byte1 + (byte) data[1];
        byte2 = (byte) data[2];
        outputRom[location] = (byte) byte1;
        outputRom[location + 1] = (byte) byte2;
    }

    /// <summary>
    /// Get the face number from the given team/position
    /// </summary>
    /// <param name="team"></param>
    /// <param name="position"></param>
    /// <returns></returns>
    public int GetFace(String team, String position)
    {
        int positionOffset = GetPositionIndex(position);
        int teamIndex = GetTeamIndex(team);
        if (positionOffset < 0 || teamIndex < 0)
        {
            errors.add(String.format("GetFace Error getting face for %s %s", team, position));
            return -1;
        }
        int loc = (teamIndex * teamAbilityOffset) + abilityOffsets[positionOffset] + billsQB1AbilityStart + 2;

        //int loc = faceOffsets[positionOffset] + faceTeamOffsets[teamIndex];
        //loc = 0x3012 + faceOffsets[positionOffset] + teamIndex*0x75;
        int ret = 0xff & outputRom[loc];
        return ret;
    }

    /// <summary>
    /// Sets the face for the guy at position 'position' on team 'team'.
    /// </summary>
    /// <param name="team"></param>
    /// <param name="position"></param>
    /// <param name="face"></param>
    public void SetFace(String team, String position, int face)
    {
        int positionOffset = GetPositionIndex(position);
        int teamIndex = GetTeamIndex(team);
        if (positionOffset < 0 || teamIndex < 0 || face < 0x00 | face > 0xD4)
        {
            errors.add(String.format("SetFace Error setting face for %s %s face=%d", team, position, face));
            if (face < 0x00 | face > 0xD4)
            {
                errors.add(String.format("Valid Face numbers are 0x00 - 0xD4"));
            }
            return;
        }
        int loc = (teamIndex * teamAbilityOffset) + abilityOffsets[positionOffset] + billsQB1AbilityStart + 2;
        int skin = 0x80;

        if (face < 0x53)
        {
            skin = 0x00;
        }

        SetCutSceneRace(teamIndex, positionOffset, face);
        outputRom[loc] = (byte) skin;//(byte)face;
    }
    private final int mRaceCutsceneStartPos = 0x17EF;

    private void SetCutSceneRace(int teamIndex, int positionIndex, int color)
    {
        int pi = (positionIndex / 8);
        int romPosition = mRaceCutsceneStartPos + pi + teamIndex * 4;
        byte oldValue = outputRom[romPosition];
        //            byte mask = GetMask(positionIndex, color);
        //            byte newValue = (byte) (oldValue & mask);
        byte newValue = GetNewValue(oldValue, positionIndex, color);
        //            For initial debugging
        //            System.Diagnostics.Debug.Assert(oldValue == newValue, "Race Error!", 
        //                String.format("teamIndex={0}, positionIndex=%d, color={2}",teamIndex, positionIndex, color));
        outputRom[romPosition] = newValue;
    }

    private byte GetNewValue(byte oldValue, int positionIndex, int race)
    {
        byte mask = (byte) 0xFF;
        byte ret = oldValue;
        int bitIndex = positionIndex % 8;
        if (race == 0)
        {
            mask = GetWhiteMask(positionIndex);
            ret = (byte) (ret & mask);
        } else
        {
            mask = GetColorMask(positionIndex);
            ret = (byte) (ret | mask);
        }
        return ret;
    }

    private byte GetWhiteMask(int positionIndex)
    {
        byte ret = (byte) 0xFF;
        int bitIndex = positionIndex % 8;
        switch (bitIndex)
        {
            case 0:
                ret = (byte) 0x7F;
                break;//01111111
            case 1:
                ret = (byte) 0xBF;
                break;//10111111
            case 2:
                ret = (byte) 0xDF;
                break;//11011111
            case 3:
                ret = (byte) 0xEF;
                break;//11101111
            case 4:
                ret = (byte) 0xF7;
                break;//11110111
            case 5:
                ret = (byte) 0xFB;
                break;//11111011
            case 6:
                ret = (byte) 0xFD;
                break;//11111101
            case 7:
                ret = (byte) 0xFE;
                break;//11111110
            }
        return ret;
    }

    private byte GetColorMask(int positionIndex)
    {
        byte ret = (byte) 0x00;
        int bitIndex = positionIndex % 8;
        switch (bitIndex)
        {
            case 0:
                ret = (byte) 0x80;
                break;//10000000
            case 1:
                ret = (byte) 0x40;
                break;//01000000
            case 2:
                ret = (byte) 0x20;
                break;//00100000
            case 3:
                ret = (byte) 0x10;
                break;//00010000
            case 4:
                ret = (byte) 0x08;
                break;//00001000
            case 5:
                ret = (byte) 0x04;
                break;//00000100
            case 6:
                ret = (byte) 0x02;
                break;//00000010
            case 7:
                ret = (byte) 0x01;
                break;//00000001
            }
        return ret;
    }

    /// <summary>
    /// Sets the return team for 'team'
    /// </summary>
    /// <param name="team"></param>
    /// <param name="pos0"></param>
    /// <param name="pos1"></param>
    /// <param name="pos2"></param>
    public void SetReturnTeam(String team, String pos0, String pos1, String pos2)
    {
        if (Index(positionNames, pos0) > -1
                && Index(positionNames, pos1) > -1
                && Index(positionNames, pos2) > -1)
        {
            InsertGuyOnReturnTeam(pos0, team, 0);
            InsertGuyOnReturnTeam(pos1, team, 1);
            InsertGuyOnReturnTeam(pos2, team, 2);
        } else
        {
            errors.add(String.format(
                    "ERROR! Invalid position on RETURN_TEAM %s %s %s ", pos0, pos1, pos2));
        }
    }

    /// <summary>
    /// Returns a String like "RETURN_TEAM WR3, RB3, RCB"
    /// </summary>
    /// <param name="team"></param>
    /// <returns></returns>
    public String GetReturnTeam(String team)
    {
        String ret = null;
        int teamIndex = Index(teams, team);
        if (teamIndex < 0)
        {
            errors.add(String.format("ERROR! GetReturnTeam.Invalid team %s", team));
        } else
        {
            int teamLocation = pr_kr_team_start_offset + (4 * teamIndex);
            int pos0, pos1, pos2;

            pos0 = 0xff & outputRom[teamLocation];
            pos1 = 0xff & outputRom[teamLocation + 1];
            pos2 = 0xff & outputRom[teamLocation + 2];

            // finish this (error checking)
            if (pos0 > -1 && pos0 < positionNames.length
                    && pos1 > -1 && pos1 < positionNames.length
                    && pos2 > -1 && pos2 < positionNames.length)
            {
                ret = String.format(
                        "RETURN_TEAM %s, %s, %s",
                        positionNames[pos0], positionNames[pos1], positionNames[pos2]);
            } else
            {
                errors.add("ERROR! Return Team Messed up in ROM.");
            }
        }
        return ret;
    }

    /// <summary>
    /// Set the punt returner by position.
    /// Hi nibble.
    /// </summary>
    /// <param name="team"></param>
    /// <param name="position"></param>
    public void SetPuntReturner(String team, String position)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) SetPuntReturner:: Invalid team %s", team));
            return;
        } else if (!IsValidPosition(position))
        {
            errors.add(String.format("ERROR! (low level) SetPuntReturner:: Invalid Position %s", position));
            return;
        }

        int index = IsGuyOnReturnTeam(position, team);
        if (index < 0)
        {
            index = 1;
            InsertGuyOnReturnTeam(position, team, index);
        }

        int teamIndex = Index(teams, team);
        int location = pr_kr_start_offset + teamIndex;
        int kr_pr = 0xff & outputRom[location];

        kr_pr = kr_pr & 0xf0;
        kr_pr = kr_pr + index;
        outputRom[location] = (byte) kr_pr;
    }

    /// <summary>
    /// Set the kick returner by position.
    /// Lo nibble.
    /// </summary>
    /// <param name="team"></param>
    /// <param name="position"></param>
    public void SetKickReturner(String team, String position)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) SetKickReturner:: Invalid team %s", team));
            return;
        } else if (!IsValidPosition(position))
        {
            errors.add(String.format("ERROR! (low level) SetKickReturner:: Invalid Position %s", position));
            return;
        }

        int index = IsGuyOnReturnTeam(position, team);
        if (index < 0)
        {
            index = 0;
            InsertGuyOnReturnTeam(position, team, index);
        }
        int teamIndex = Index(teams, team);
        int location = pr_kr_start_offset + teamIndex;
        int kr_pr = 0xff & outputRom[location];
        kr_pr = kr_pr & 0x0f;
        kr_pr += (index << 4);
        outputRom[location] = (byte) kr_pr;
    }

    /// <summary>
    /// Gets the position who returns punts.
    /// </summary>
    /// <param name="team"></param>
    /// <returns></returns>
    public String GetKickReturner(String team)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) GetKickReturner:: Invalid team %s", team));
            return null;
        }

        String ret = "";
        //int location = 0x328d3 + Index(teams,team);
        int teamIndex = Index(teams, team);
        int returnTeamIndex = 0xff & outputRom[pr_kr_start_offset + teamIndex] >> 4;
        int teamLocation = pr_kr_team_start_offset + (4 * teamIndex);

        int positionIndex = 0xff & outputRom[ returnTeamIndex + teamLocation];

        if (positionIndex < positionNames.length)
        {
            ret = positionNames[positionIndex];
        }
        return ret;

        /*
         int b = 0xff & outputRom[loc1];
         b = b & 0x0F;
         ret = positionNames[b];
         return ret;
         */
    }

    /// <summary>
    /// 
    /// </summary>
    /// <param name="guy">the position name of a guy</param>
    /// <param name="team">the team</param>
    /// <returns></returns>
    private int IsGuyOnReturnTeam(String guy, String team)
    {
        int ret = -1;
        int teamIndex = Index(teams, team);
        int posIndex = Index(positionNames, guy);
        int teamLocation = pr_kr_team_start_offset + (4 * teamIndex);

        if (outputRom[teamLocation] == (byte) posIndex)
        {
            ret = 0;
        } else if (outputRom[teamLocation + 1] == (byte) posIndex)
        {
            ret = 1;
        } else if (outputRom[teamLocation + 2] == (byte) posIndex)
        {
            ret = 2;
        }

        return ret;
    }

    private void InsertGuyOnReturnTeam(String position, String team, int index)
    {
        int teamIndex = Index(teams, team);
        int posIndex = Index(positionNames, position);

        if (index < 0 || index > 2 || teamIndex < 0 || teamIndex > 27 || posIndex < 0)
        {
            errors.add(String.format(
                    "InsertGuyOnReturnTeam: invalid arguments %s, %s, %d", position, team, index));
            return;
        }

        int teamLocation = pr_kr_team_start_offset + (4 * teamIndex);
        outputRom[teamLocation + index] = (byte) posIndex;
    }

    /// <summary>
    /// Gets the position who returns kicks.
    /// </summary>
    /// <param name="team"></param>
    /// <returns></returns>
    public String GetPuntReturner(String team)
    {
        if (!IsValidTeam(team))
        {
            errors.add(String.format("ERROR! (low level) GetPuntReturner:: Invalid team %s", team));
            return null;
        }

        String ret = "";
        int teamIndex = Index(teams, team);
        int returnTeamIndex = outputRom[(pr_kr_start_offset + teamIndex)] & 0x0f;
        int teamLocation = pr_kr_team_start_offset + (4 * teamIndex);

        int positionIndex = 0xff & outputRom[ returnTeamIndex + teamLocation];

        if (positionIndex < positionNames.length)
        {
            ret = positionNames[positionIndex];
        }
        return ret;
        /*
         String ret = "";
         int location = 0x328d3 + Index(teams,team);
         int b = 0xff & outputRom[location];
         b = b & 0xF0;
         b = b >> 4;
         ret = positionNames[b];
         return ret;
         */
    }

    public void SetQuarterLength(byte len)
    {
        if (outputRom != null)
        {
            outputRom[ QUARTER_LENGTH] = len;
        }
    }

    public byte GetQuarterLength()
    {
        byte ret = 0;
        if (outputRom != null)
        {
            ret = outputRom[ QUARTER_LENGTH];
        }
        return ret;
    }

    public String GetBytes(int location, int length)
    {
        String ret;
        int lastByte = location + length;
        if (lastByte > outputRom.length)
        {
            ret = "ERROR! location + length > rom length";
        } else
        {
            StringBuilder builder = new StringBuilder(2 * length + 3);
            builder.append("0x");
            for (int i = location; i < lastByte; i++)
            {
                builder.append(String.format("%02x", 0xff & outputRom[i]));
            }
            ret = builder.toString();
        }
        return ret;
    }
    private Pattern simpleSetRegex;

    public void ApplySet(String line)
    {
        if (simpleSetRegex == null)
        {
            simpleSetRegex = Pattern
                    .compile("SET\\s*\\(\\s*(0x[0-9a-fA-F]+)\\s*,\\s*(0x[0-9a-fA-F]+)\\s*\\)");
        }

        if (simpleSetRegex.matcher(line).matches())
        {
            ApplySimpleSet(line);
        } else if (line.indexOf("PromptUser") > -1)
        {
            if (line.indexOf(getRomVersion()) > -1)
            {
                /*
                 * good to go! apply it String simpleSetLine =
                 * StringInputDlg.PromptForSetUserInput(line); if
                 * (!string.IsNullOrEmpty(simpleSetLine)) {
                 * ApplySet(simpleSetLine); }
                 */
            } else
            {
                MainClass.ShowError("Rom version not specified in Hack: "
                        + line);
            }
        } else
        {
            errors.add(String.format("ERROR with line \"%s\"", line));
        }
    }

    protected void ApplySimpleSet(String line)
    {
        if (simpleSetRegex == null)
        {
            simpleSetRegex = Pattern
                    .compile("SET\\s*\\(\\s*(0x[0-9a-fA-F]+)\\s*,\\s*(0x[0-9a-fA-F]+)\\s*\\)");
        }

        Matcher m = simpleSetRegex.matcher(line);
        if (!m.find())
        {
            MainClass.ShowError(String.format(
                    "SET function not used properly. incorrect syntax>'%s'",
                    line));
            return;
        }
        String loc = m.group(1).toString().toLowerCase();
        String val = m.group(2).toString().toLowerCase();
        loc = loc.substring(2);
        val = val.substring(2);
        if (val.length() % 2 != 0)
        {
            val = "0" + val;
        }

        try
        {
            int location = Integer.parseInt(loc, 16);
            byte[] bytes = GetHexBytes(val);
            if (location + bytes.length > outputRom.length)
            {
                MainClass
                        .ShowError(String
                        .format("ApplySet:> Error with line %s. Data falls off the end of rom.\n",
                        line));
            } else if (location < 0)
            {
                MainClass
                        .ShowError(String
                        .format("ApplySet:> Error with line %s. location is negative.\n",
                        line));
            } else
            {
                for (int i = 0; i < bytes.length; i++)
                {
                    outputRom[location + i] = bytes[i];
                }
            }
        } catch (Exception e)
        {
            MainClass
                    .ShowError(String.format(
                    "ApplySet:> Error with line %s.\n%s", line,
                    e.getMessage()));
        }
    }

    private byte[] GetHexBytes(String input)
    {
        if (input == null)
        {
            return null;
        }

        byte[] ret = new byte[input.length() / 2];
        String b = "";
        int tmp = 0;
        int j = 0;

        for (int i = 0; i < input.length(); i += 2)
        {
            b = input.substring(i, i + 2);
            tmp = Integer.parseInt(b, 16);
            ret[j++] = (byte) tmp;
        }
        return ret;
    }
    /// <summary>
    /// Returns the first index of element that occurs in 'array'. returns
    /// -1 if 'element' doesn't occur in 'array'.
    /// </summary>
    /// <param name="array"></param>
    /// <param name="element"></param>
    /// <returns></returns>

    private int Index(String[] array, String element)
    {
        for (int i = 0; i < array.length; i++)
        {
            if (array[i].equals(element))
            {
                return i;
            }
        }

        return -1;
    }

    private void PrintValidAbilities()
    {
        errors.add(String.format(
                "Valid player abilities are 6, 13, 19, 25, 31, 38, 44, 50, 56, 63, 69, 75, 81, 88, 94, 100"));
    }

    public String StringifyArray(int[] input)
    {
        if (input == null)
        {
            return null;
        }

        StringBuilder sb = new StringBuilder(40);
        for (int i = 0; i < input.length; i++)
        {
            sb.append(String.format("%d, ", input[i]));
        }

        int index = sb.length() - 2;
        sb.delete(index, index + 1); // trim last comma
        return sb.toString();
    }

    /// <summary>
    /// Returns an Vector<String> of errors that were encountered during the operation.
    /// </summary>
    /// <param name="scheduleList"></param>
    /// <returns></returns>
    public Vector<String> ApplySchedule(Vector<String> scheduleList)
    {
        if (scheduleList != null && outputRom != null)
        {
            SNES_ScheduleHelper sch = new SNES_ScheduleHelper(outputRom);
            sch.ApplySchedule(scheduleList);
            Vector<String> errors = sch.GetErrorMessages();
            return errors;
        }
        return null;
    }
    private int mBillsUniformLoc = 0x2c2e4;// this is for the NES version

    public void SetHomeUniform(String team, String colorString)
    {
        int loc = GetUniformLoc(team);
        if (loc > -1)
        {
            //                OutputRom[loc]     = pantsColor;
            //                OutputRom[loc + 2] = jerseyColor;
        }
    }

    public void SetAwayUniform(String team, String colorString)
    {
        int loc = GetUniformLoc(team);
        if (loc > -1)
        {
            //                OutputRom[loc + 3] = pantsColor;
            //                OutputRom[loc + 5] = jerseyColor;
        }
    }

    public String GetHomeUniform(String team)
    {
        String ret = "";
        int loc = GetUniformLoc(team);
        if (loc > -1)
        {
            //                ret = String.format("Home=0x{0:x2}{1:x2}",
            //                    OutputRom[loc], 
            //                    OutputRom[loc + 2] );
        }
        return ret;
    }

    public String GetAwayUniform(String team)
    {
        String ret = "";
        int loc = GetUniformLoc(team);
        if (loc > -1)
        {
            //                ret = String.format("Away=0x{0:x2}{1:x2}",
            //                    OutputRom[loc + 3], 
            //                    OutputRom[loc + 5] );
        }
        return ret;
    }

    protected int GetUniformLoc(String team)
    {
        int ret = -1;
        int teamIndex = GetTeamIndex(team);
        if (teamIndex > -1 && teamIndex < 28)
        {
            ret = mBillsUniformLoc + (teamIndex * 0xa);
        }
        return ret;
    }

    public String GetGameUniform(String team)
    {
        String ret = "";
        return ret;
    }

    public void SetDivChampColors(String team, String colorString)
    {
    }

    public void SetUniformUsage(String team, String usage)
    {
    }

    public String GetUniformUsage(String team)
    {
        return "";
    }

    public void SetConfChampColors(String team, String colorString)
    {
    }

    public String GetDivChampColors(String team)
    {
        String ret = "";
        return ret;
    }

    public String GetConfChampColors(String team)
    {
        String ret = "";
        return ret;
    }

    public String GetChampColors(String team)
    {
        String ret = "";
        return ret;
    }
}