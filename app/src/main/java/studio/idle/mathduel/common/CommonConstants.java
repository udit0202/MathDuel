package studio.idle.mathduel.common;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Udit on 26/03/15.
 */
public class CommonConstants {
    public static final String PREF_FILE_NAME = "Settings";
    public static final String GAME_LEVEL = "gameLevel";
    public static final String PLAYER_ONE_NAME = "playerOneName";
    public static final String PLAYER_TWO_NAME = "playerTwoName";
    public static final String GAME_DURATION = "gameDuration";
    public static final String IS_SOUND_ON = "isSoundOn";

    public static List<String> symbolListEasy = Arrays.asList("+", "-");
    public static List<String> symbolListMedium = Arrays.asList("+", "+", "-", "-", "*", "/");
    public static List<String> symbolListDifficult = Arrays.asList("+", "-", "*","+", "-", "*", "/");

    public static int minEasyValue = 0;
    public static int minMediumValue = 0;
    public static int minHardValue = 0;
    public static int maxEasyValue = 100;
    public static int maxMediumValue = 500;
    public static int maxHardValue = 5000;

    public static enum MusicPlayerState {
        Stopped,
        Playing,
        Paused
    }

    public static enum GoalSide {
        PlayerOneSide,
        PlayerTwoSide
    }

    public enum difficultyLevels {
        easy,
        medium,
        hard;

        public static difficultyLevels get(int code) {

            CommonConstants.difficultyLevels level = easy;
            switch(code) {
                case  0: level = easy;
                    break;
                case  1: level = medium;
                    break;
                case  2: level = hard;
            }

            return level;
        }
    }
}
