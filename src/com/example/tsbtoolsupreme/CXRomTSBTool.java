package com.example.tsbtoolsupreme;

import java.io.File;
import java.io.IOException;
import java.util.*;

// defect happens when saving player attributes for 49ers +
// need to find out where 49ers QB1 attributes start. -=> 0x3CDC
/*
 cxrom posted this a couple weeks ago:

 this is all for the NFC-West:

 the numbers in "()" are length per team and "[]" is total length for all 4 teams

 0x199C1 - sim data ($30)[$C0]
 0x23FF0 - large helmet palettes ($08)[$20]
 0x27FDB - run/pass ratio ($01)[$04]
 0x2CF82 - in game jersey colors ($0A)[$28]
 0x348F7 - action sequence palettes ($08)[$20]
 0x34953 - division champ screen palettes ($05)[$14]
 0x349D2 - conference champ screen palettes ($04)[$10]

 */
/// <summary>
/// Summary description for CXRomTSBTool.
///   Still having problems with playbooks.
///   
/// Done:
/// 1. Names and numbers
/// 2. normal attributes.
/// 3. Faces
/// 4. player Sim attributes
/// 5. team sim attributes
/// 6. Team Playbooks.
/// 7. team offensive preference.
/// 8. team offensive formation
/// </summary>
public class CXRomTSBTool extends TecmoTool
{
    // extra teams (team index)
    // fortyNiners = 0x1E, rams = 0x1F, seahawks = 0x20, cardinals = 0x21

    // extra teams' name pointers occur at 0x3EB0
    // find out where Attributes are for new teams.
    private final int FORTY_NINERS_PLAYBOOK_START = 0x1D390;
    private boolean DoSchedule = true;
    // player sim attributes
    //0x199BF - 0x199C6: pointers to sim attributes (8 bytes:2 each)
    //0x199C1 - 0x19A80: sim attributes (all teams)
    int fortyNinersQB1SimAttrStart = 0x199C1;//0x199C7;
    int fortyNinersRESimLoc = 0x199D9;//0x199DF;
    //final int fortyNinersRunPassPreferenceLoc = 0x27526; // defect
    private int fortyNinersRunPassPreferenceLoc = 0x27fdb;
    private int FORTY_NINERS_QB1_POINTER = 0x3eb0;

    private final int ROM_SIZE_v1_05 = 0x80010;
    private final int ROM_SIZE_v1_11 = 0xc0010; // 786448;
    /* v1.11 
     * if( outputRom.length == 786448){
     *   FORTY_NINERS_QB1_POINTER = 0x3e054
     *   mGetDataPositionOffset = 0x36010;
     * }
     * TODO:
     * Currently needs fixing v1.11: 
     *   1. Offensive formations (all)
     */
    private int mGetDataPositionOffset =  0x30000 + 0x010;
    private int FORTY_NINERS_KR_PR_LOC = 0x32cb2;
    private int FORTY_NINERS_KR_PR_LOC_1 = 0x3F93C;
    private int m_ExpansionSegmentEnd = 0x3fff0;
    private byte[] m_RomVersionData = null;

    /// <summary>
    /// Returns the rom version 
    /// </summary>
    @Override
    public String getRomVersion()
    {
        if( ROM_LENGTH == ROM_SIZE_v1_11)
            return "32TeamNES_CXROM_v1.11";
        else
            return "32TeamNES_CXROM_v1.05";
    }

    public CXRomTSBTool()
    {
    }

    public CXRomTSBTool(String fileName)
    {
        
        Init(fileName);
    }

    private void SetupForCxROM()
    {
        if( outputRom.length == ROM_SIZE_v1_11)
        {
            /* Version 1.11*/
            FORTY_NINERS_QB1_POINTER = 0x3e054;
            mGetDataPositionOffset = 0x36010;
            fortyNinersRunPassPreferenceLoc = 0x27fd6;
            ROM_LENGTH = ROM_SIZE_v1_11;
            //KR/PR 
            FORTY_NINERS_KR_PR_LOC   = 0x32CC7;
            FORTY_NINERS_KR_PR_LOC_1 = 0x7FD51;            
        }
        else
        {
            /* Version 1.05*/
            ROM_LENGTH = ROM_SIZE_v1_05;
        }
        
        mTeamFormationsStartingLoc = 0x3f940;
        namePointersStart = 0x48 + 12;
        lastPointer = 0x06e4;//0x6d9 + 12;

        faceTeamOffsets = new int[]
        {
            0x3012, 0x3087, 0x30FC, 0x3171, 0x31E6, 0x325B, 0x32D0, 0x3345, 0x33BA, 0x342F, 0x34A4, 0x3519, 0x358e, 0x3603,
            0x384C, 0x36ed, 0x3762, 0x37D7, 0x3678, 0x38C1, 0x3936, 0x39AB, 0x3A20, 0x3A95, 0x3B0A, 0x3B7F, 0x3BF4, 0x3C69, 0x384C, 0x36ed, 0x3762, 0x37D7, 0x3678, 0x38C1
        };
    }

    boolean mAddedFormationRomError=false;
    /// <summary>
    /// Sets the team's offensive formation.
    /// </summary>
    /// <param name="team"></param>
    /// <param name="formation"></param>
    @Override
    public void SetTeamOffensiveFormation(String team, String formation)
    {
        if( outputRom.length == ROM_SIZE_v1_11)
        {
            if(!mAddedFormationRomError)
            {
                mAddedFormationRomError = true;
                getErrors().add("Setting offensive formation on CXROM_v1.11 ROM is not yet supported.");
            }
            return;
        }
        int teamIndex = GetTeamIndex(team);
        if (teamIndex > -1 && teamIndex < 34)
        {
            int location = mTeamFormationsStartingLoc + teamIndex;
//                int location2 = mTeamFormationsStartingLoc2 + teamIndex;

            if (formation.equals(m2RB_2WR_1TE))
            {
                outputRom[location] = (byte) 0x00;
                //outputRom[location2] = (byte) 0x00;
            } else if (formation.equals(m1RB_3WR_1TE))
            {
                outputRom[location] = (byte) 0x02;
                //outputRom[location2] = (byte) 0x02;
            } else if (formation.equals(m1RB_4WR))
            {
                outputRom[location] = (byte) 0x01;
                //outputRom[location2] = (byte) 0x01;
            } else
            {
                getErrors().add(String.format(
                        "ERROR! Formation %s for team %s is invalid.",
                        formation, team));
                getErrors().add(String.format(
                        "  Valid formations are:\n  %s\n  %s\n  %s",
                        m2RB_2WR_1TE, m1RB_3WR_1TE, m1RB_4WR));
            }

        } else
        {
            getErrors().add(String.format("ERROR! Team '%s' is invalid, Offensive Formation not set", team));
        }
    }

     /**
     * 
     * @param len the length of the ROM
     * @return true if it's the correct length, false otherwise.
     */
    @Override
    public boolean IsValidRomSize(long len)
    {
        boolean ret = false;
        if( len == ROM_SIZE_v1_05 )
        {
            /* Version 1.05*/
            ret = true;
        }
        else if( len == ROM_SIZE_v1_11)
        {
            /* Version 1.11*/
            //FORTY_NINERS_QB1_POINTER = 0x3e054;
            //mGetDataPositionOffset = 0x36010;
            ret = true;
        }
        return ret;
    }
    
    @Override
    public boolean ReadRom(String filename)
    {
        boolean ret = false;
        ret = super.ReadRom(filename);
        if (ret)
        {
            SetupForCxROM();
            m_RomVersionData = new byte[14];
            for (int i = 0; i < m_RomVersionData.length; i++)
            {
                m_RomVersionData[i] = outputRom[i + m_ExpansionSegmentEnd];
            }
        }
        return ret;
    }

    private boolean CheckROMVersion()
    {
        boolean ret = true;
        if (outputRom.length > m_ExpansionSegmentEnd + 20)
        {
            for (int i = 0; i < m_RomVersionData.length; i++)
            {
                if (outputRom[i + m_ExpansionSegmentEnd] != m_RomVersionData[i])
                {

                    ret = false;
                    break;
                }
            }
        }
        return ret;
    }

    /// <summary>
    /// Check to see if we overwrote any ROM data after the end of the expansion
    /// name segment. If we are in GUI mode, prompt the user to confirm that they want to save the
    /// data.
    /// </summary>
    /// <param name="filename"></param>
    @Override
    public void SaveRom(String filename) throws IOException
    {
        if (CheckROMVersion())
        {
            super.SaveRom(filename);
        } else
        {
            MainClass.ShowError(
                    "WARNING!! Expansion team name section has been overwritten, ROM could be messed up.");
            /*if( MainClass.GUI_MODE )
             {
             if( MessageBox.Show(null,  "ROM could be messed up, do you want to save anyway?", "ERROR!",
             MessageBoxButtons.YesNo, MessageBoxIcon.Question) == DialogResult.Yes)
             {
             super.SaveRom(filename);
             }
             }
             else*/
            {
                super.SaveRom(filename);
            }
        }
    }

    /// <summary>
    /// Gets the position who returns punts.
    /// </summary>
    /// <param name="team"></param>
    /// <returns></returns>
    @Override
    public String GetPuntReturner(String team)
    {
        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            return super.GetPuntReturner(team);
        } else
        {
            String ret = "";
            int location = FORTY_NINERS_KR_PR_LOC + teamIndex - 30;
            int b = 0xff & outputRom[location];
            b = b & 0x0F;
            ret = positionNames[b];
            return ret;
        }
    }

    /// <summary>
    /// Gets the position who returns kicks.
    /// </summary>
    /// <param name="team"></param>
    /// <returns></returns>
    @Override
    public String GetKickReturner(String team)
    {
        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            return super.GetKickReturner(team);
        } else
        {
            String ret = "";
            int location = FORTY_NINERS_KR_PR_LOC + teamIndex - 30;
            int b = 0xff & outputRom[location];
            b = b & 0xF0;
            b = b >> 4;
            ret = positionNames[b];
            return ret;
        }
    }

    @Override
    public void SetPuntReturner(String team, String position)
    {
        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            super.SetPuntReturner(team, position);
            return;
        } else
        {
            int location = FORTY_NINERS_KR_PR_LOC + GetTeamIndex(team) - 30;
            int location1 = FORTY_NINERS_KR_PR_LOC_1 + GetTeamIndex(team) - 30;
            //switch(position)
            {
                if (position.equals("QB1")
                        || position.equals("QB2")
                        || position.equals("C")
                        || position.equals("LG")
                        || // these guys can return punts/kicks too.
                        position.equals("RB1") || position.equals("RB2") || position.equals("RB3")
                        || position.equals("RB4") || position.equals("WR1")
                        || position.equals("WR2") || position.equals("WR3")
                        || position.equals("WR4") || position.equals("TE1")
                        || position.equals("TE2"))
                {
                    int pos = Index(positionNames, position);
                    int b = 0xff & outputRom[location];
                    b = b & 0xF0;
                    b = b + pos;
                    outputRom[location] = (byte) b;
                    outputRom[location1] = (byte) b;
                } else
                {
                    getErrors().add(String.format("Cannot assign '%s' as a punt returner", position));
                }
            }
        }
    }

    @Override
    public void SetKickReturner(String team, String position)
    {
        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            super.SetKickReturner(team, position);
            return;
        } else
        {
            int location = FORTY_NINERS_KR_PR_LOC + GetTeamIndex(team) - 30;
            int location2 = FORTY_NINERS_KR_PR_LOC_1 + GetTeamIndex(team) - 30;
            if (position.equals("QB1")
                    || position.equals("QB2")
                    || position.equals("C")
                    || position.equals("LG")
                    || // these guys can return punts/kicks too.
                    position.equals("RB1") || position.equals("RB2") || position.equals("RB3")
                    || position.equals("RB4") || position.equals("WR1") || position.equals("WR2")
                    || position.equals("WR3") || position.equals("WR4") || position.equals("TE1")
                    || position.equals("TE2"))
            {
                int pos = Index(positionNames, position);
                int b = 0xff & outputRom[location];
                b = b & 0x0F;
                b = b + (pos << 4);
                outputRom[location] = (byte) b;
                outputRom[location2] = (byte) b;
            } else
            {
                getErrors().add(String.format("Cannot assign '%s' as a kick returner", position));
            }

        }
    }

    @Override
    public String GetAll() throws Exception
    {
        String team;
        StringBuilder all = new StringBuilder(30 * 41 * positionNames.length);
        String year = String.format("YEAR=%s\n", GetYear());
        all.append(year);
        int normalTeamEnd = 28;
        for (int i = 0; i < normalTeamEnd; i++)
        {
            team = Teams[i];
            all.append(GetTeamPlayers(team));
        }
        String expansionTeams = GetExpansionTeams();
        all.append(expansionTeams);

        return all.toString();
    }

    private String GetExpansionTeams() throws Exception
    {
        StringBuilder ret = new StringBuilder(2000);

        ret.append(GetTeamPlayers(Teams[30])); // fortyNiners
        ret.append(GetTeamPlayers(Teams[31])); // rams
        ret.append(GetTeamPlayers(Teams[32])); // seahawks
        ret.append(GetTeamPlayers(Teams[33])); // cardinals

        String result = ret.toString();
        return result;
    }

    /// <summary>
    /// Sets the team sim offense tendency . 
    /// 00 = Little more rushing, 01 = Heavy Rushing, 
    /// 02 = little more passing, 03 = Heavy Passing. 
    /// </summary>
    /// <param name="team">the team name</param>
    /// <param name="val">the number to set it to.</param>
    /// <returns>true if set, fales if could not set it.</returns>
    @Override
    public boolean SetTeamSimOffensePref(String team, int val)
    {
        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            return super.SetTeamSimOffensePref(team, val);
        }

        if (val > -1 && val < 4 && teamIndex != -1)
        {
            int loc = fortyNinersRunPassPreferenceLoc + teamIndex - 30;
            outputRom[loc] = (byte) val;
        } else
        {
            if (teamIndex != -1)
            {
                getErrors().add(String.format("Can't set offensive pref to '%d' valid values are 0-3.\n", val));
            } else
            {
                getErrors().add(String.format("Team '%s' is invalid\n", team));
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
    @Override
    public int GetTeamSimOffensePref(String team)
    {
        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            return super.GetTeamSimOffensePref(team);
        }

        int val = -1;
        if (teamIndex > -1)
        {
            int loc = fortyNinersRunPassPreferenceLoc + teamIndex - 30;
            val = 0xff & outputRom[loc];
        } else
        {
            getErrors().add(String.format("Team '%s' is invalid\n", team));
        }
        return val;
    }

    @Override
    protected int GetOffensivePlayerSimDataLocation(String team, String position)
    {
        int location = -4;

        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            location = super.GetOffensivePlayerSimDataLocation(team, position);
        } else if (teamIndex > 29)
        {

            int positionIndex = GetPositionIndex(position);
            location = (teamIndex - 30) * teamSimOffset + (positionIndex * 2) + fortyNinersQB1SimAttrStart;
        }
        return location;
    }

    @Override
    protected int GetDefinsivePlayerSimDataLocation(String team, String position)
    {
        int location = -4;

        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            location = super.GetDefinsivePlayerSimDataLocation(team, position);
        } else if (teamIndex > 29)
        {
            int positionIndex = GetPositionIndex(position);
            location = (teamIndex - 30) * teamSimOffset + (positionIndex - 17) + fortyNinersRESimLoc;
        }
        return location;
    }

    @Override
    protected int GetPunkKickSimDataLocation(int teamIndex)
    {
        int ret = -1;

        if (teamIndex < 28)
        {
            ret = super.GetPunkKickSimDataLocation(teamIndex);
        } else
        {
            ret = (teamIndex - 30) * teamSimOffset + fortyNinersQB1SimAttrStart + 0x2E;
        }

        return ret;
    }

    /// <summary>
    /// Returns the simulation data for the given team.
    /// Simulation data is of the form '0xNN' where N is a number 1-F (hex).
    /// A team's sim data of '0x57' signifies that the team has a simulation figure of
    /// '5' for offense, and '7' for defense.
    /// </summary>
    /// <param name="team">The team of interest</param>
    /// <returns></returns>
    @Override
    public byte GetTeamSimData(String team)
    {
        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            return super.GetTeamSimData(team);
        }

        if (teamIndex > 29 && teamIndex < 34)
        {
            int location = (teamIndex - 30) * teamSimOffset + fortyNinersQB1SimAttrStart + 0x2f;
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
    @Override
    public void SetTeamSimData(String team, byte values)
    {
        if (!IsValidTeam(team))
        {
            getErrors().add(String.format("ERROR! (low level) SetTeamSimData:: team %s is invalid ", team));
            return;
        }

        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            super.SetTeamSimData(team, values);
        } else
        {
            // not yet implemented in cxrom's rom
            int location = (teamIndex - 30) * teamSimOffset + fortyNinersQB1SimAttrStart + 0x2f;
            int currentValue = 0xff & outputRom[location];
            outputRom[location] = values;
            currentValue = 0xff & outputRom[location];
        }
    }
    /// <summary>
    /// Gets the point in the player number name data that a player's data begins.
    /// </summary>
    /// <param name="team"></param>
    /// <param name="position"></param>
    /// <returns></returns>

    @Override
    public int GetDataPosition(String team, String position) throws Exception
    {
        int ret = -1;
        if (!IsValidTeam(team) || !IsValidPosition(position))
        {
            throw new Exception(
                    String.format("ERROR! (low level) GetDataPosition:: either team %s or position %s is invalid.", team, position));
        }
        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            return super.GetDataPosition(team, position);
        }
        if (teamIndex > 29)
        {
            int positionIndex = GetPositionIndex(position);
            // the players total index (QB1 bills=0, QB2 bills=2 ...)
            int pointerLocation = 0;

            pointerLocation = (teamIndex - 30) * 0x3c + FORTY_NINERS_QB1_POINTER
                    + (positionIndex * 2);

            byte lowByte = outputRom[pointerLocation];
            int hiByte = 0xff & outputRom[pointerLocation + 1];
            hiByte = hiByte << 8;
            hiByte = hiByte + (0xff & lowByte);

            //int ret = hiByte - 0x8000 + 0x010;
            //ret = hiByte + 0x30000 + 0x010;
            ret = hiByte + mGetDataPositionOffset;
        }
        return ret;
    }

    /// <summary>
    /// Get the starting point of the guy AFTER the one passed to this method.
    /// This is hacked up to work with CXROM's rom.
    /// </summary>
    /// <param name="team"></param>
    /// <param name="position"></param>
    /// <returns></returns>
    @Override
    public int GetNextDataPosition(String team, String position) throws Exception
    {
        int pointerLocation = 0;
        int teamIndex = GetTeamIndex(team);

        if (teamIndex > 29 && position.equals("P"))
        {
            pointerLocation = FORTY_NINERS_QB1_POINTER + 0x3c + (teamIndex - 30) * 0x3c;
        } 
        else if (teamIndex > 29)
        {
            int positionIndex = GetPositionIndex(position) + 1;
            pointerLocation = (teamIndex - 30) * 0x3c + FORTY_NINERS_QB1_POINTER
                    + (positionIndex * 2);
        }

        if (pointerLocation != 0)
        {
            byte lowByte = outputRom[pointerLocation];
            int hiByte = 0xff & outputRom[pointerLocation + 1];
            hiByte = hiByte << 8;
            hiByte = hiByte + (0xff & lowByte);

            //int ret = hiByte - 0x8000 + 0x010;
            //int ret = hiByte + 0x30000 + 0x010;
            int ret = hiByte + mGetDataPositionOffset;
            return ret;
        }
        return super.GetNextDataPosition(team, position);
    }

    @Override
    protected int GetPointerPosition(String team, String position) throws Exception
    {
        int ret = -4;
        if (!IsValidTeam(team) || !IsValidPosition(position))
        {
            throw new Exception(
                    String.format("ERROR! (low level) GetPointerPosition:: either team %s or position %s is invalid.", team, position));
        }
        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            return super.GetPointerPosition(team, position);
        }
        if (teamIndex > 29)
        {
            int positionIndex = GetPositionIndex(position);
            int playerSpot = (teamIndex - 30) * positionNames.length + positionIndex;
            if (positionIndex < 0)
            {
                getErrors().add(String.format("ERROR! (low level) Position '%s' does not exist. Valid positions are:", position));
                for (int i = 1; i <= positionNames.length; i++)
                {
                    System.err.printf("%s\t", positionNames[i - 1]);
                }
                return -1;
            }
            ret = FORTY_NINERS_QB1_POINTER + (2 * playerSpot);
        }
        return ret;
    }

    @Override
    protected void ShiftDataAfter(String team, String position, int shiftAmount) throws Exception
    {
        int teamIndex = GetTeamIndex(team);
        if (teamIndex == 27 && position.equals("P"))
        {
            return;
        }

        if (teamIndex < 28)
        {
            super.ShiftDataAfter(team, position, shiftAmount);
            return;
        }

        if (!IsValidTeam(team) || !IsValidPosition(position))
        {
            throw new Exception(
                    String.format("ERROR! (low level) ShiftDataAfter:: either team %s or position %s is invalid.", team, position));
        }

        if (team == Teams[Teams.length - 1] && position.equals("P"))
        {
            return;
        }

        int startPosition = this.GetNextDataPosition(team, position);
        int endPosition = m_ExpansionSegmentEnd - 17;// -17 to compensate for shifting down

        if (shiftAmount < 0)
        {
            ShiftDataUp(startPosition, endPosition, shiftAmount, outputRom);
        } else if (shiftAmount > 0)
        {
            ShiftDataDown(startPosition, endPosition, shiftAmount, outputRom);
        }
    }

    @Override
    protected void AdjustDataPointers(int pos, int change)
    {
        if (pos == lastPointer - 2)//0x06e2) // panther's punter
        {
            int pointerLoc = pos + 2;
            byte lo = outputRom[pointerLoc];
            byte hi = outputRom[pointerLoc + 1];
            int pVal = 0xff & hi;
            pVal = pVal << 8;
            pVal += 0xff & lo;
            pVal += change;

            lo = (byte) (pVal & 0x00ff);
            pVal = pVal >> 8;
            hi = (byte) pVal;
            outputRom[pointerLoc] = lo;
            outputRom[pointerLoc + 1] = hi;
        } else if (pos < lastPointer + 1)
        {
            super.AdjustDataPointers(pos, change);
        } else
        {
            byte low, hi;
            int word;
            // last pointer is at 0x69d For NES
            // snes is lastpointer+1 (0x178738+1)

            int start = pos + 2;
            int i = 0;
            int end = //-1;
                    //CARDINALS_PUNTER_POINTER + 2; // cards are team #33
                    FORTY_NINERS_QB1_POINTER + 0x3c + (33 - 30) * 0x3c;

            for (i = start; i < end; i += 2)
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
    }

    /// <summary>
    /// Get the face number from the given team/position
    /// </summary>
    /// <param name="team"></param>
    /// <param name="position"></param>
    /// <returns></returns>
    @Override
    public int GetFace(String team, String position)
    {
        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            return super.GetFace(team, position);
        }
        int positionOffset = GetPositionIndex(position);

        if (positionOffset < 0 || teamIndex < 0)
        {
            getErrors().add(String.format("GetFace Error getting face for %s %s", team, position));
            return -1;
        }
        teamIndex -= 2;
        int loc = 0x3012 + faceOffsets[positionOffset] + teamIndex * 0x75;
        int ret = 0xff & outputRom[loc];
        return ret;
    }

    /// <summary>
    /// Sets the face for the guy at position 'position' on team 'team'.
    /// </summary>
    /// <param name="team"></param>
    /// <param name="position"></param>
    /// <param name="face"></param>
    @Override
    public void SetFace(String team, String position, int face)
    {
        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            super.SetFace(team, position, face);
            return;
        }
        int positionOffset = GetPositionIndex(position);

        if (positionOffset < 0 || teamIndex < 0 || face < 0x00 | face > 0xD4)
        {
            getErrors().add(String.format("SetFace Error setting face for %s %s face=%d", team, position, face));
            if (face < 0x00 | face > 0xD4)
            {
                getErrors().add(String.format("Valid Face numbers are 0x00 - 0xD4"));
            }
            return;
        }
        teamIndex -= 2;
        int loc = 0x3012 + faceOffsets[positionOffset] + teamIndex * 0x75;
        outputRom[loc] = (byte) face;
    }

    @Override
    protected int GetAttributeLocation(int teamIndex, int posIndex)
    {
        int location = -1;
        if (teamIndex < 28)
        {
            location = super.GetAttributeLocation(teamIndex, posIndex);
        } else
        {
            location = super.GetAttributeLocation(teamIndex - 2, posIndex);
        }
        return location;
    }

    /// <summary>
    /// Returns an Vector<String> of errors that were encountered during the operation.
    /// </summary>
    /// <param name="scheduleList"></param>
    /// <returns></returns>
    @Override
    public Vector<String> ApplySchedule(Vector<String> scheduleList)
    {
        if (scheduleList != null && outputRom != null)
        {
            CXRomScheduleHelper sch = new CXRomScheduleHelper(outputRom);
            sch.ApplySchedule(scheduleList);
            Vector<String> errors = sch.GetErrorMessages();
            return errors;
        }
        return null;
    }

    @Override
    protected int GetPlaybookLocation(int team_index)
    {
        if (team_index < 28)
        {
            return super.GetPlaybookLocation(team_index);
        } else
        {
            team_index -= 30;
            return FORTY_NINERS_PLAYBOOK_START + team_index * 4;
        }
    }

    @Override
    public String GetSchedule()
    {
        String ret = "";
        if (outputRom != null && DoSchedule)
        {
            CXRomScheduleHelper sh2 = new CXRomScheduleHelper(outputRom);
            ret = sh2.GetSchedule();
            Vector<String> errors = sh2.GetErrorMessages();
            if (errors != null && getErrors().size() > 0)
            {
                MainClass.ShowErrors(errors);
            }
        }

        return ret;
    }
    //#region Uniform Color Stuff
    private int mFortyNinersUniformLoc = 0x2cf82;

    @Override
    protected int GetUniformLoc(String team)
    {
        int ret = -1;
        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            ret = super.GetUniformLoc(team);
        } else
        {
            teamIndex -= 30;
            ret = mFortyNinersUniformLoc + (teamIndex * 0xa);
        }
        return ret;
    }
    private int mFortyNinersActionSeqLoc = 0x348f7;
    /// <summary>
    /// Gets the location of the given team's uniform data.
    /// </summary>
    /// <param name="team"></param>
    /// <returns>The location of the given team's uniform data, -1 on error</returns>

    @Override
    protected int GetActionSeqUniformLoc(String team)
    {
        int ret = -1;
        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            ret = super.GetActionSeqUniformLoc(team);
        } else
        {
            teamIndex -= 30;
            ret = mFortyNinersActionSeqLoc + (teamIndex * 0x8);
        }
        return ret;
    }
    private int m49ersDivChampLoc = 0x3494f;//; 0x34953;

    @Override
    protected int GetDivChampLoc(String team)
    {
        int ret = -1;
        int teamIndex = GetTeamIndex(team);
        if (teamIndex < 28)
        {
            ret = super.GetDivChampLoc(team);
        } else
        {
            teamIndex -= 30;
            ret = m49ersDivChampLoc + (teamIndex * 0x5);
        }

        return ret;
    }
    private int m49ersConfChampLoc = 0x349cf;//0x349D2;

    @Override
    protected int GetConfChampLoc(String team)
    {
        int ret = -1;
        int teamIndex = GetTeamIndex(team);

        if (teamIndex < 28)
        {
            ret = super.GetConfChampLoc(team);
        } else
        {
            teamIndex -= 30;
            ret = m49ersConfChampLoc + (teamIndex * 0x4);
        }
        return ret;
    }
    //#endregion
}
