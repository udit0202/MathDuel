package studio.idle.mathduel.common;

import android.util.Log;

import java.util.Random;

/**
 * Created by Udit on 31/03/15.
 */
public class OptionsHelper {

    public static WrongOptionsObject fetchWrongOptions(int answer, int gameLevel, String operand) {
        WrongOptionsObject wrongOptions = new WrongOptionsObject();
        wrongOptions.setWrongOptionOne(answer);
        wrongOptions.setWrongOptionTwo(answer);

        Random rand = new Random();

        int numberRange, numberRangeModified, numberOffset, randOperator;

        if(answer >= 0 && answer <= 100) {
            numberRange = answer / 10 + 1; // Minimum 1
            while( wrongOptions.getWrongOptionOne() == answer) {
                Log.i("Checking", "First while loop" + " " + answer);
                numberOffset = rand.nextInt(2 * numberRange + 1) + (-1 * numberRange); // {- numberRange to + numberRange}
                wrongOptions.setWrongOptionOne(answer + numberOffset);
            }

            while( wrongOptions.getWrongOptionTwo() == answer ||
                    wrongOptions.getWrongOptionTwo() == wrongOptions.getWrongOptionOne()) {
                Log.i("Checking", "Second while loop" + " " + answer + " "+ wrongOptions.getWrongOptionOne());
                numberOffset = rand.nextInt(2 * numberRange + 1) + (-1 * numberRange); // {- numberRange to + numberRange}
                wrongOptions.setWrongOptionTwo(answer + numberOffset);
            }
        } else {
            numberRange = Math.abs(answer / 10 );
            numberRangeModified = (numberRange % 5) + 1;
            while( wrongOptions.getWrongOptionOne() == answer) {
                Log.i("Checking", "First while loop else" + " " + answer);
                if(gameLevel == 0) {
                    numberOffset = (rand.nextInt(2 * numberRangeModified + 1) + (-1 * numberRangeModified)) * 10 + (rand.nextInt(3) + (-1));
                } else {
                    numberOffset = (rand.nextInt(2 * numberRangeModified + 1) + (-1 * numberRangeModified)) * 10 ; // don't offset it
                }
                wrongOptions.setWrongOptionOne( answer + numberOffset);
            }

            while( wrongOptions.getWrongOptionTwo() == answer ||
                    wrongOptions.getWrongOptionTwo() == wrongOptions.getWrongOptionOne()) {
                Log.i("Checking", "Second while loop else" + " " + answer + " "+ wrongOptions.getWrongOptionOne());
                if(gameLevel != 2) {
                    numberOffset = (rand.nextInt(2 * numberRangeModified + 1) + (-1 * numberRangeModified)) * 10 + (rand.nextInt(3) + (-1));
                } else {
                    numberOffset = (rand.nextInt(2 * numberRangeModified + 1) + (-1 * numberRangeModified)) * 10; // don't offset it
                }
                wrongOptions.setWrongOptionTwo( answer + numberOffset);
            }
        }
        return wrongOptions;
    }
}
