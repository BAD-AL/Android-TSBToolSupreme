package com.example.tsbtoolsupreme;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


public interface ITecmoTool
    {
        byte[] getOutputRom();
        void setOutputRom(byte[] data);

        boolean getShowOffPref();
        
        String[] getPositionNames();
        
        void setShowOffPref(boolean pref);

        Vector<String> getErrors();
        void setErrors(Vector<String> errors);

        String GetKey();

        String GetAll()throws Exception;
        
        String GetTeamOffensiveFormation(String team);
        byte GetTeamSimData(String team);
        int GetTeamSimOffensePref(String team);
        String GetPlaybook(String team);

        String GetPlayerStuff(boolean jerseyNumbers,boolean names,boolean faces,boolean abilities,boolean simData) throws Exception;

        String GetSchedule();

        void SaveRom(String filename) throws  IOException;

        void ApplySet(String line);
                
        String GetBytes(int location, int length);

        void SetPlaybook(String team, String runs, String passes);

        boolean ApplyJuice(int week,int amount);

        void SetTeamSimData(String team, byte data);

        boolean SetTeamSimOffensePref(String team, int val);

        void SetTeamOffensiveFormation(String team, String formation);

        void SetYear(String year);

        boolean IsValidPosition(String position);

        void SetFace(String team, String position, int face);

        void InsertPlayer(String currentTeam,String pos,String fname,String lname,byte jerseyNumber) throws Exception ;

        void SetQBAbilities(String team, 
            String qb, 
            int runningSpeed, 
            int rushingPower, 
            int maxSpeed,
            int hittingPower,
            int passingSpeed,
            int passControl,
            int accuracy, 
            int avoidPassBlock
            );

        void SetQBSimData(String team, String pos, int[] data);

        void SetSkillPlayerAbilities(String team, 
            String pos, 
            int runningSpeed, 
            int rushingPower, 
            int maxSpeed,
            int hittingPower,
            int ballControl,
            int receptions
            );

        void SetSkillSimData(String team, String pos, int[] data);

        void SetOLPlayerAbilities(String team, 
            String pos, 
            int runningSpeed, 
            int rushingPower, 
            int maxSpeed,
            int hittingPower );

        void SetDefensivePlayerAbilities(String team, 
            String pos, 
            int runningSpeed, 
            int rushingPower, 
            int maxSpeed,
            int hittingPower,
            int passRush,
            int interceptions
            );

        void SetDefensiveSimData(String team, String pos, int[] data);

        void SetKickPlayerAbilities(String team, 
            String pos, 
            int runningSpeed, 
            int rushingPower, 
            int maxSpeed,
            int hittingPower,
            int kickingAbility,
            int avoidKickBlock
            );
        void SetPuntingSimData(String team, int data);

        void SetKickingSimData(String team, int data);

        void SetKickReturner(String team, String position);

        void SetPuntReturner(String team, String position);

        Vector<String> ApplySchedule( Vector<String> scheduleList );

        void SetReturnTeam(String team, String pos0, String pos1, String pos2);

        boolean Init(String fileName);

        void SetHomeUniform(String team, String colorString);

        void SetAwayUniform(String team, String colorString);
        
        String GetGameUniform(String team);

        void SetUniformUsage(String team, String usage);

        String GetUniformUsage(String team);

        void SetDivChampColors(String team, String colorString);

        void SetConfChampColors(String team, String colorString);

        String GetDivChampColors(String team );

        String GetConfChampColors(String team );

        String GetChampColors(String team);

        /// <summary>
        /// "SNES", "28TeamNES", "32TeamNES"
        /// </summary>
        String getRomVersion();
        
        boolean IsValidRomSize(long fileLength);
        
        public String GetPlayerData(String team, String position);

    }
