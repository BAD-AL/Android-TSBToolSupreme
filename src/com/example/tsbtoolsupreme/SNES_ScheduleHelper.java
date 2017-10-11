package com.example.tsbtoolsupreme;


import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


    /// NOTE: byte swapping 
    ///    0x12345678 
    ///        |
    ///        ---> 0x78563412
    ///
    /// <summary>
    /// Summary description for ScheduleHelper.
    /// </summary>
    public class SNES_ScheduleHelper
    {
        private final int weekOneStartLoc = 0x15f3be;//0x329db;
        private Vector<String> errors;
        private int[] teamGames;

        int week, week_game_count,total_game_count;
         private Pattern gameRegex = Pattern.compile("([0-9a-z]+)\\s+at\\s+([0-9a-z]+)");


        private int[] gamesPerWeek = 
            {14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14};
        private byte[] outputRom;
        
        public SNES_ScheduleHelper(byte[] outputRom)
        {
            this.outputRom = outputRom;
            errors = new Vector<String>();
        }

        private void CloseWeek()
        {
            if( week > -1 )
            {
                int i = week_game_count;
                while( i < 14 )
                {
                    ScheduleGame(0xff,0xff,week, i /*week_game_count*/);
                    i++;
                }
            }
            week++;
            total_game_count += week_game_count;
            week_game_count = 0;
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

            if( SNES_TecmoTool.AUTO_CORRECT_SCHEDULE )
            {
                lines = Ensure18Weeks(lines);
            }

            String line;
            for(int i =0; i < lines.size(); i++)
            {
                line = lines.get(i).trim().toLowerCase();
                if( line.startsWith("#") || line.length() < 3)
                { // do nothing.
                }
                else if(line.startsWith("week"))
                {
                    if(week > 18)
                    {
                        errors.add("Error! You can have only 18 weeks in a season.");
                        break;
                    }
                    CloseWeek();
                    System.out.printf("Scheduleing %s\n",line);
                }
                else 
                {
                    ScheduleGame(line);
                }
            }
            CloseWeek();// close week 18

            if( week < 18 )
            {
                errors.add("Warning! You didn't schedule all 18 weeks. The schedule could be messed up.");
            }
            if( teamGames != null)
            {
                for( int i = 0;  i < teamGames.length; i++)
                {
                    if( teamGames[i] != 16 )
                    {
                        errors.add(String.format(
                            "Warning! The %s have %d games scheduled.", 
                            TecmoTool.GetTeamFromIndex(i), teamGames[i] ));
                    }
                }
            }
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
                if( week_game_count > 13 )
                {
                    errors.add(String.format(
                        "Error! Week %d: You can have no more than 14 games in a week.",week+1));
                    ret = false;
                }
                else if( ScheduleGame(awayTeam, homeTeam, week, week_game_count) )
                {
                    week_game_count++;
                    ret = true;
                }
                
            }
            if( total_game_count + week_game_count > 224 )
            {
                errors.add(String.format(
                    "Warning! Week %d: There are more than 224 games scheduled.",week+1));
            }
            return ret;
        }


        /// <summary>
        /// 
        /// </summary>
        /// <param name="awayTeam"></param>
        /// <param name="homeTeam"></param>
        /// <param name="week">Week is 0-16 (0 = week 1).</param>
        /// <param name="gameOfWeek"></param>
        public boolean ScheduleGame(String awayTeam, String homeTeam, int week, int gameOfWeek)
        {
            int awayIndex = TecmoTool.GetTeamIndex(awayTeam);
            int homeIndex = TecmoTool.GetTeamIndex(homeTeam);
            
            if( awayIndex == -1 || homeIndex == -1 )
            {
                errors.add(String.format("Error! Week %d: Game '%s at %s'", week+1, awayTeam, homeTeam ));
                return false;
            }

            if( awayIndex == homeIndex && awayIndex < 28 )
            {
                errors.add(String.format(
                    "Warning! Week %d: The %s are scheduled to play against themselves.",week+1, awayTeam ));
            }

            if(week < 0 || week > 17){
                errors.add(String.format("Week %d is not valid. Weeks range 1 - 18.",week+1));
                return false;
            }
            if( GameLocation(week,gameOfWeek) < 0 ){
                errors.add(String.format("Game %d for week %d is not valid. Valid games for week %d are 0-%d.",
                    gameOfWeek,week,gamesPerWeek[week]-1));
                errors.add(String.format("%s at %s",awayTeam, homeTeam));
            }

            ScheduleGame(awayIndex, homeIndex, week, gameOfWeek);

            if( awayTeam .equals( "null") || homeTeam .equals( "null"))
                return false;
            return true;
        }

        private void ScheduleGame(int awayTeamIndex, int homeTeamIndex, int week, int gameOfWeek)
        {
            int location = GameLocation(week,gameOfWeek);
            if(location > 0)
            {
                outputRom[location]   = (byte)awayTeamIndex;
                outputRom[location+1] = (byte)homeTeamIndex;
                if( awayTeamIndex < 28)
                {
                    IncrementTeamGames(awayTeamIndex);
                    IncrementTeamGames(homeTeamIndex);
                }
            }
            /*else
            {
                errors.add(String.format("INVALID game for ROM. Week=%d Game of Week =%d",
                    week,gameOfWeek);
            }*/
        }

        /// <summary>
        /// Returns a String like "49ers at giants", for a valid week, game of week combo.
        /// </summary>
        /// <param name="week">The week in question.</param>
        /// <param name="gameOfWeek">The game to get.</param>
        /// <returns>Returns a String like "49ers at giants", for a valid week, game of week combo, returns null
        /// upon error. </returns>
        public String GetGame(int week, int gameOfWeek)
        {
            int location = GameLocation(week,gameOfWeek);
            if(location == -1)
                return null ;
            int awayIndex = outputRom[location];
            int homeIndex = outputRom[location+1];
            String ret = "";

            if( awayIndex < 28 )
            {
                ret = String.format("%s at %s", 
                    TecmoTool.GetTeamFromIndex(awayIndex), 
                    TecmoTool.GetTeamFromIndex(homeIndex));
            }
            return ret;
        }

        /// <summary>
        /// Returns a week from the season.
        /// </summary>
        /// <param name="week">The week to get [0-16] (0= week 1).</param>
        /// <returns></returns>
        public String GetWeek(int week)
        {
            if(week < 0 || week > gamesPerWeek.length-1)
                return null;
            StringBuilder sb = new StringBuilder(20*14);
            sb.append(String.format("WEEK %d\n",week+1));

            String game;

            for(int i = 0; i < gamesPerWeek[week]; i++)
            {
                game = GetGame(week,i);
                if( game != null && game.length() > 0 )
                {
                    sb.append(String.format("%s\n",game));
                }
            }
            sb.append("\n");
            return sb.toString();
        }

        public String GetSchedule()
        {
            StringBuilder sb = new StringBuilder(20*14*18);
            //sb.append("Schedule\n\n");
            for(int week = 0; week < gamesPerWeek.length; week++)
                sb.append(GetWeek(week));

            return sb.toString();
        }

        private int GameLocation(int week, int gameOfweek)
        {
            if( week < 0 || week > gamesPerWeek.length-1 || 
                gameOfweek > gamesPerWeek[week] || gameOfweek < 0)
                return -1;

            int offset = 0;
            for(int i = 0; i < week; i++)
                offset += (gamesPerWeek[i]*2);

            offset += gameOfweek*2;
            int location = weekOneStartLoc+ offset;
            return location;
        }

        public Vector<String> GetErrorMessages()
        {
            return errors;
        }

        
        private void IncrementTeamGames(int teamIndex)
        {
            if( teamGames == null )
                teamGames = new int[28];
            //Console.WriteLine("IncrementTeamGames team index = "+teamIndex);
            if( teamIndex < teamGames.length )
                teamGames[teamIndex]++;

        }

        private Vector<String> Ensure18Weeks(Vector<String> lines )
        {

            int wks = CountWeeks(lines);
            String line1, line2;
            for( int i = lines.size()-2; i > 0; i-=2 )
            {
                line1 = lines.get(i);
                line2 = lines.get(i+1);
                if(wks > 17)
                {
                    break;
                }
                else if( line1.indexOf("at") > -1 && line2.indexOf("at") > -1 )
                {
                    lines.insertElementAt( "WEEK ", i+1);
                    i--;
                    wks++;
                }
            }

            //if( MainClass.GUI_MODE )
            //    ShowLines(lines);
            return lines;
        }

        private int CountWeeks(Vector<String> lines)
        {
            int count = 0;
            String line ="";
            int vSize = lines.size();
            for(int i =0; i < vSize; i++)
            {
                line= lines.get(i);
                if( line.toLowerCase().indexOf("week") > -1)
                    count++;
            }
            return count;
        }

    }