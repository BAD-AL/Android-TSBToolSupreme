
package com.example.tsbtoolsupreme;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


    /// <summary>
    /// Summary description for SchedulerHelper2.
    /// </summary>
    public class ScheduleHelper2
    {
        protected int weekOneStartLoc      = 0x329db;
        protected int end_schedule_section = 0x3400e;
        protected int gamesPerWeekStartLoc = 0x329c9;
        protected int weekPointersStartLoc = 0x329a7; // you need to swap these bytes
        private int[] teamGames;
        protected int total_games_possible = 238;
        protected int gamePerWeekLimit = 14;
        protected int totalGameLimit = 224;
        protected int totalWeeks = 17;

        private int week             = -1;
        private int week_game_count  =  0;
        private int total_game_count =  0;

        private Vector<String> messages;
        private byte[] outputRom;
        private Pattern gameRegex;

        protected  void AddMessage(String message)
        {
            if( message != null && message.length() > 0 )
                messages.add(message);
        }

        /// <summary>
        /// 
        /// </summary>
        public int getTotalGameCount()
        {
            return total_game_count; 
        }

        public ScheduleHelper2(byte[] outputRom)
        {
            this.outputRom = outputRom;
            gameRegex = Pattern.compile( "([0-9a-z]+)\\s+at\\s+([0-9a-z]+)");
        }

        /// <summary>
        /// Applies a schedule to the rom.
        /// </summary>
        /// <param name="lines">the contents of the schedule file.</param>
        public void ApplySchedule(Vector<String> lines)
        {
            week             = -1;
            week_game_count  =  0;
            total_game_count =  0;
            messages         = new Vector<String>(50);

            String line;
            for(int i =0; i < lines.size(); i++)
            {
                line = lines.get(i).trim().toLowerCase();
                try
                {
                    if( line.startsWith("#") || line.length() < 3)
                    { // do nothing.
                    }
                    else if(line.startsWith("week"))
                    {
                        if(week > totalWeeks-1 /*17*/)
                        {
                            AddMessage("Error! You can have only 17 weeks in a season.");
                            break;
                        }
                        SetupWeek();
                        System.out.printf("Scheduleing %s \n",line);
                    }
                    else 
                    {
                        ScheduleGame(line);
                    }
                }
                catch(Exception e)
                {
                    System.out.printf("Exception! with line '%s' %s\n %s %n",line, e.getMessage(), e.getStackTrace());
                    AddMessage("Error on line '"+ line +"'");
                }
            }
            ClosePrevWeek(); // close off last week.
            if( week < totalWeeks-1 )
            {
                AddMessage("Warning! You didn't schedule all 17 weeks. The schedule could be messed up.");
            }
            if( teamGames != null)
            {
                for( int i = 0;  i < teamGames.length; i++)
                {
                    if( teamGames[i] != 16 ) 
                    {
                        AddMessage(String.format(
                            "Warning! The %s have %d games scheduled.", 
                            TecmoTool.GetTeamFromIndex(i), teamGames[i] ));

                    }
                }
            }
        }

        private void SetupWeek()
        {
            ClosePrevWeek();
            week++;
            total_game_count += week_game_count;
            week_game_count = 0;
            SetupPointerForCurrentWeek();
        }

        private void ClosePrevWeek()
        {
            if( week > -1 )
            {
                int location = gamesPerWeekStartLoc + week;
                outputRom[location] = (byte) week_game_count;
                if( week_game_count == 0)
                {
                    AddMessage(String.format("ERROR! Week %d. You need at least 1 game in each week.", week+1));
                }
            }
        }

        private void SetupPointerForCurrentWeek()
        {
            if( week == 0) 
                return;
            int val      = ( 2 * total_game_count) + 0x89cb;
            int location = weekPointersStartLoc + (week * 2);
            if( week < 17 )
            {
                outputRom[location+1]   = (byte) (val >> 8);
                outputRom[location] = (byte) (val & 0x00ff);
            }
            else
            {
                AddMessage(String.format("ERROR! To many Weeks %d",week +1));
            }
        }

        /// <summary>
        /// Attempts to schedule a game.
        /// </summary>
        /// <param name="awayTeam">Away team's name.</param>
        /// <param name="homeTeam">Home team's name.</param>
        /// <returns> true on success, false on failure.</returns>
        protected  boolean ScheduleGame(String awayTeam, String homeTeam)
        {
            boolean ret = false;
            int awayIndex = TecmoTool.GetTeamIndex(awayTeam);
            int homeIndex = TecmoTool.GetTeamIndex(homeTeam);

            if( awayIndex == -1 || homeIndex == -1)
            {
                AddMessage(String.format("Error! Week %d: Game '%s at %s'", week+1 , awayTeam, homeTeam));
                return false;
            }

            if( awayIndex == homeIndex )
            {
                AddMessage(String.format(
                    "Warning! Week %d: The %s are scheduled to play against themselves.",week+1, awayTeam ));
            }
            
            int location = weekOneStartLoc + ((week_game_count + total_game_count) * 2);
            if( location >= weekOneStartLoc && location < end_schedule_section )
            {
                outputRom[location]   = (byte) awayIndex;
                outputRom[location+1] = (byte) homeIndex;
                IncrementTeamGames(awayIndex);
                IncrementTeamGames(homeIndex);
                ret = true;
            }
            return ret;
        }


        /// <summary>
        /// Attempts to schedule a game.
        /// </summary>
        /// <param name="line"></param>
        /// <returns>True on success, false on failure.</returns>
        private boolean ScheduleGame(String line)
        {
            boolean ret = false;
            
            Matcher m = gameRegex.matcher(line);
            String awayTeam, homeTeam;

            if( m.find() )
            {
                awayTeam = m.group(1).toString();
                homeTeam = m.group(2).toString();
                if( week_game_count > gamePerWeekLimit - 1 )
                {
                    AddMessage(String.format(
                        "Error! Week %d: You can have no more than %d games in a week.",week+1, gamePerWeekLimit));
                    ret = false;
                }
                else if( ScheduleGame(awayTeam, homeTeam) )
                {
                    week_game_count++;
                    ret = true;
                }
                else
                {
                    //AddMessage(String.Format("Error scheduling game '{0}' for week {1}.", line, week+1));
                }
            }
            if( total_game_count + week_game_count > totalGameLimit )
            {
                AddMessage(String.format(
                    "Warning! Week %d: There are more than %d games scheduled.",week+1,gamePerWeekLimit));
            }
            return ret;
        }

        /// <summary>
        /// Gets the Schedule.
        /// </summary>
        /// <returns></returns>
        public String GetSchedule()
        {
            StringBuffer sb = new StringBuffer(17*28*12);
            for( int i =0; i < 17; i++)
            {
                sb.append(String.format("WEEK %d\n",(i+1)));
                sb.append(GetWeek(i)+"\n");
            }
            return sb.toString();
        }

        /// <summary>
        /// Gets the schedule for week 'week'.
        /// </summary>
        /// <param name="week">The week to get.(Zero-based)</param>
        /// <returns>The week as a String. </returns>
        public String GetWeek(int week)
        {
            if( week < 0 || week > totalWeeks-1 )
            {
                AddMessage("Programming Error! 'GetWeek' Week must be in the range 0-16.");
                return null;
            }

            StringBuffer sb = new StringBuffer(14*12);
            int gamesInWeek = GetGamesInWeek(week);
            //sb.Append(gamesInWeek + " games \r\n");
            int prevGames = 0;
            for(int i = 0; i < week; i++)
            {
                prevGames += GetGamesInWeek(i);
            }
            int gameLocation = weekOneStartLoc + ( 2 * prevGames );
            for(int i = 0; i < gamesInWeek; i++)
            {
                sb.append(String.format("%s", GetGame( gameLocation +(2*i) ) ) );
            }
            return sb.toString();
        }

        /// <summary>
        /// Returns 
        /// </summary>
        /// <param name="romLocation"></param>
        /// <returns></returns>
        public String GetGame( int romLocation )
        {
            // TODO fix the upperbound.
            if( romLocation < weekOneStartLoc /*|| romLocation > weekOneStartLoc + 450*/)
            {
                AddMessage(String.format(
                 "Programming ERROR! GetGame Invalid Game Location '0x%x'. Valid locations are 0x%x-0x%x.",
                    romLocation,weekOneStartLoc, weekOneStartLoc+448 ) );
                return null;
            }
            int away = 0xff & outputRom[romLocation];
            int home = 0xff & outputRom[romLocation+1];

            String awayTeam = TecmoTool.GetTeamFromIndex(away);
            String homeTeam = TecmoTool.GetTeamFromIndex(home);

            String ret = awayTeam + " at " + homeTeam +"\n"; 
                    //String.print("{0} at {1}\n", awayTeam, homeTeam);
            return ret;
        }

        /// <summary>
        /// Returns the number of games in the given week.
        /// </summary>
        /// <param name="week"></param>
        /// <returns></returns>
        public int GetGamesInWeek(int week)
        {
            if( week < 0 || week > totalWeeks-1)
            {
                AddMessage( "Programming Error! GetGamesInWeek Week "+week+" is invalid. Week range = 0-16." );
                return -1;
            }
            int result = 0xff & outputRom[gamesPerWeekStartLoc+week];
            return result;
        }


        private void IncrementTeamGames(int teamIndex)
        {
            if( teamGames == null )
                teamGames = new int[TecmoTool.Teams.length];
            teamGames[teamIndex]++;
        }

        /// <summary>
        /// Returns an Vector<String> of error messages encountered when processing the schedule data.
        /// </summary>
        /// <returns></returns>
        public Vector<String> GetErrorMessages()
        {
            return messages;
        }
    }
