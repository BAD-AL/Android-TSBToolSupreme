
package com.example.tsbtoolsupreme;


    /// <summary>
    /// Summary description for CXRomScheduleHelper.
    /// </summary>
    public class CXRomScheduleHelper extends ScheduleHelper2
    {
        public CXRomScheduleHelper(byte[] outputRom)  
        {
                super(outputRom);
                //weekOneStartLoc      = 0x329db;
                end_schedule_section = 0x3400e;
                //gamesPerWeekStartLoc = 0x329c9;
                weekPointersStartLoc = 0x329a7;
                total_games_possible = 16*16;
                gamePerWeekLimit = 16;
                totalGameLimit = 16*16;
                //totalWeeks = 17;
        }

        @Override
        protected void AddMessage(String message)
        {
            if( message.indexOf("AFC") == -1 && message.indexOf("NFC") == -1 )
            {
                super.AddMessage (message);
            }
        }


        @Override
        protected boolean ScheduleGame(String awayTeam, String homeTeam)
        {
            boolean ret = false;
            if( getTotalGameCount() < total_games_possible )
            {
                ret = super.ScheduleGame (awayTeam, homeTeam);
            }
            else
            {
                AddMessage(String.format(
                    "ERROR! maximum game limit reached (%d) {%s} at {%s} will not be scheduled",
                    total_games_possible ,awayTeam, homeTeam));
            }
            return ret;
        }


    }