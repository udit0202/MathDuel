package studio.idle.mathduel;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import studio.idle.mathduel.common.CommonConstants;


public class GameActivity extends Activity {

    public static enum MusicPlayerState {
        Stopped,
        Playing,
        Paused
    }

    public static enum GoalSide {
        PlayerOneSide,
        PlayerTwoSide
    }

    CountDownTimer timer;
    MusicPlayerState mState;
    int gameLevel;
    String playerOneName, playerTwoName;
    RelativeLayout vsOverlayView, mainView;
    ImageView footballView;
    boolean lock = false, gameOver;
    int answer, playerOneScore = 0, playerTwoScore = 0;
    MediaPlayer mp;
    int gameDuration, tickTime = 1000; // 1 second
    int timeLeft;

    Map<Integer, Boolean> firstMap = new HashMap<Integer, Boolean>();
    Map<Integer, Boolean> secondMap = new HashMap<Integer, Boolean>();
    Map<Integer, Integer> mappingMapFirst = new HashMap<Integer, Integer>();
    Map<Integer, Integer> mappingMapSecond = new HashMap<Integer, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Hide the action bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        //Setting the game level
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            gameLevel = extras.getInt(CommonConstants.GAME_LEVEL);
            playerOneName = extras.getString(CommonConstants.PLAYER_ONE);
            playerTwoName = extras.getString(CommonConstants.PLAYER_TWO);
            gameDuration = extras.getInt(CommonConstants.GAME_DURATION);
            /*TextView temp = (TextView) findViewById(R.id.temp_text_view);
            temp.setText(CommonConstants.difficultyLevels.get(gameLevel).name());*/
        } else { // Just as a backup setting game level as medium
            gameLevel = 1;
        }

        InitializeMappingMap();

        TextView playerOneVsView = (TextView) findViewById(R.id.playerOneVsView);
        TextView playerTwoVsView = (TextView) findViewById(R.id.playerTwoVsView);
        playerOneVsView.setText(playerOneName);
        playerTwoVsView.setText(playerTwoName);

        TextView timerText = (TextView) findViewById(R.id.countDownTimer);
        TextView timerText2 = (TextView) findViewById(R.id.countDownTimer2);
        Typeface custom_font = Typeface.createFromAsset(getResources().getAssets(), "fonts/digital_font.ttf");
        timerText.setTypeface(custom_font);
        timerText2.setTypeface(custom_font);

        playSound();

        vsOverlayView = (RelativeLayout) findViewById(R.id.vsOverlayView);
        mainView = (RelativeLayout) findViewById(R.id.mainScreenLayout);
        ObjectAnimator anim = ObjectAnimator.ofFloat(vsOverlayView, "alpha", 1f, 0f);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(mainView, "alpha", 0f, 1f);

        anim2.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                TextView counter321One = (TextView) findViewById(R.id.counter321_one);
                TextView counter321Two = (TextView) findViewById(R.id.counter321_two);

                for (int i = 3; i >= 1; i--) {
                    counter321One.setText(String.valueOf(i));
                    counter321Two.setText(String.valueOf(i));
                }

                startCountdown(gameDuration, tickTime);
                setFormula();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        anim.setDuration(8000);
        anim2.setDuration(8000);
        anim.start();
        anim2.start();
    }

    private void startCountdown(int totalTime, int tickTime) {

        timer = new CountDownTimer(totalTime, tickTime) {

            TextView timerText = (TextView) findViewById(R.id.countDownTimer);
            TextView timerText2 = (TextView) findViewById(R.id.countDownTimer2);

            public void onTick(long millisUntilFinished) {
                timeLeft = (int) millisUntilFinished;
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                String minutesString;
                String secondStrings;
                minutesString = (minutes < 10 ? ("0" + minutes) : String.valueOf(minutes));
                secondStrings = (seconds < 10 ? ("0" + seconds) : String.valueOf(seconds));
                timerText.setText(minutesString + " : " + secondStrings);
                timerText2.setText(minutesString + " : " + secondStrings);
            }

            public void onFinish() {
                timerText.setText("Done");
                timerText2.setText("Done");
                gameOver();
            }
        }.start();
    }

    private void gameOver() {
        gameOver = true;
        mainView.setClickable(false);

        TextView gameOverText = (TextView) findViewById(R.id.gameOverString);
        gameOverText.setVisibility(View.VISIBLE);
        /*try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        RelativeLayout gameOverLayout = (RelativeLayout) findViewById(R.id.gameOverLayout);
        gameOverLayout.setClickable(true);
        gameOverLayout.setVisibility(View.VISIBLE);
        ObjectAnimator anim = ObjectAnimator.ofFloat(gameOverLayout, "alpha", 0, 1);
        anim.setDuration(500);
        anim.start();
        TextView winnerNameView = (TextView) findViewById(R.id.winnerName);
        String winnerName = (playerOneScore > playerTwoScore ? "A" : "B");
        winnerNameView.setText(winnerName + " Wins");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        timer.cancel();
        if (mState == MusicPlayerState.Playing) {
            mp.pause();
            mState = MusicPlayerState.Paused;
        }
        RelativeLayout backButtonPressedLayout = (RelativeLayout) findViewById(R.id.backPressedLayout);
        backButtonPressedLayout.setVisibility(View.VISIBLE);
        backButtonPressedLayout.setClickable(true);
    }

    @Override
    protected void onPause() {
        Log.i("Udit", "onPause2");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i("Udit", "onStop2");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i("Udit", "onDestroy2");
        super.onDestroy();
    }

    private void playSound() {
        mp = MediaPlayer.create(GameActivity.this, R.raw.cheer);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.release();
                mState = MusicPlayerState.Stopped;
            }
        });
        mp.start();
        mState = MusicPlayerState.Playing;
    }

    private void playGoalSound() {
        mp = MediaPlayer.create(GameActivity.this, R.raw.goal);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.start();
    }

    public void setFormula() {
        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        switch (gameLevel) {
            case 0:
                createFormula(CommonConstants.minEasyValue, CommonConstants.maxEasyValue, CommonConstants.symbolListEasy);
                break;
            case 1:
                createFormula(CommonConstants.minMediumValue, CommonConstants.maxMediumValue, CommonConstants.symbolListMedium);
                break;
            case 2:
                createFormula(CommonConstants.minHardValue, CommonConstants.maxHardValue, CommonConstants.symbolListDifficult);
        }
    }

    private void createFormula(int minValue, int maxValue, List<String> symbolList) {
        String operand = fetchOperand(symbolList);
        int firstOperator = fetchFirstOperator(operand, maxValue, minValue);//= rand.nextInt((maxValue - minValue) + 1) + minValue;
        int secondOperator = fetchSecondOperator(operand, firstOperator, maxValue, minValue);//rand.nextInt((maxValue - minValue) + 1)+ minValue;
        showFormula(firstOperator, secondOperator, operand);
        calculateAnswer(firstOperator, secondOperator, operand);
        showOptions();
        lock = false;
    }

    private void showOptions() {
        Random rand = new Random();
        Vector<Integer> vector = new Vector<>();
        vector.add(0);
        vector.add(1);
        vector.add(2);

        int correctBoxIndex = rand.nextInt(3); // For 0-2
        vector.remove(correctBoxIndex);
        int incorrectBoxIndex1 = vector.get(0);
        int incorrectBoxIndex2 = vector.get(1);

        firstMap.put(mappingMapFirst.get(correctBoxIndex), true);
        Button button = (Button) findViewById(mappingMapFirst.get(correctBoxIndex));
        button.setText(String.valueOf(answer));
        Button buttonTwo = (Button) findViewById(mappingMapFirst.get(incorrectBoxIndex1));
        buttonTwo.setText(String.valueOf(1));
        Button buttonThree = (Button) findViewById(mappingMapFirst.get(incorrectBoxIndex2));
        buttonThree.setText(String.valueOf(2));
        secondMap.put(mappingMapSecond.get(correctBoxIndex), true);
        Button button2 = (Button) findViewById(mappingMapSecond.get(correctBoxIndex));
        button2.setText(String.valueOf(answer));
        Button buttonTwo2 = (Button) findViewById(mappingMapSecond.get(incorrectBoxIndex1));
        buttonTwo2.setText(String.valueOf(1));
        Button buttonThree2 = (Button) findViewById(mappingMapSecond.get(incorrectBoxIndex2));
        buttonThree2.setText(String.valueOf(2));
    }

    private void InitializeMappingMap() {
        mappingMapFirst.put(0, R.id.optionOne);
        mappingMapFirst.put(1, R.id.optionTwo);
        mappingMapFirst.put(2, R.id.optionThree);
        mappingMapSecond.put(0, R.id.optionOne2);
        mappingMapSecond.put(1, R.id.optionTwo2);
        mappingMapSecond.put(2, R.id.optionThree2);

        firstMap.put(R.id.optionOne, false);
        firstMap.put(R.id.optionTwo, false);
        firstMap.put(R.id.optionThree, false);
        secondMap.put(R.id.optionOne2, false);
        secondMap.put(R.id.optionTwo2, false);
        secondMap.put(R.id.optionThree2, false);
    }

    private void calculateAnswer(int firstOperator, int secondOperator, String operand) {
        switch (operand) {
            case "+":
                answer = firstOperator + secondOperator;
                break;
            case "-":
                answer = firstOperator - secondOperator;
                break;
            case "*":
                answer = firstOperator * secondOperator;
                break;
            case "/":
                answer = firstOperator / secondOperator;
        }
    }

    private void showFormula(int firstOperator, int secondOperator, String operand) {
        String formulaString = firstOperator + " " + operand + " " + secondOperator;
        Button formulaButtonOne = (Button) findViewById(R.id.questionViewOne);
        Button formulaButtonTwo = (Button) findViewById(R.id.questionViewOne2);
        formulaButtonOne.setText(formulaString);
        formulaButtonTwo.setText(formulaString);
    }

    private int fetchSecondOperator(String operand, int firstOperator, int maxValue, int minValue) {
        Random rand = new Random();
        if (operand.equals("-") && gameLevel == 0) {
            return rand.nextInt((firstOperator - minValue) + 1) + minValue;
        } else if (operand.equals("*")) {
            return rand.nextInt(((firstOperator / 10) - minValue) + 1) + minValue;
        } else if (operand.equals("/")) {
            int number = rand.nextInt((firstOperator - minValue) + 1) + minValue + 1;

            while (firstOperator % number != 0) {
                number = rand.nextInt((firstOperator - minValue) + 1) + minValue + 1;
            }

            return number;
        } else {
            return rand.nextInt((maxValue - minValue) + 1) + minValue;
        }
    }

    private int fetchFirstOperator(String operand, int maxValue, int minValue) {
        Random rand = new Random();
        if (operand.equals("/")) {
            return rand.nextInt((maxValue - minValue) + 1) + (minValue + 2); // Minimum value to come as 2
        } else {
            return rand.nextInt((maxValue - minValue) + 1) + minValue;
        }
    }

    private String fetchOperand(List<String> symbolList) {
        Random rand = new Random();
        return symbolList.get(rand.nextInt((symbolList.size() - 1) + 1));
    }

    public void throwBall(final GoalSide goalSide) {
        footballView = (ImageView) findViewById(R.id.football);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int height = size.y;
        int width = size.x;
        final int movement;
        if (goalSide == GoalSide.PlayerTwoSide) {
            movement = (height / 2) * -1;
        } else {
            movement = (height / 2);
        }
        Random rand = new Random();
        int xMovement = rand.nextInt(width/10) - (width/20) ;
        ObjectAnimator anim = ObjectAnimator.ofFloat(footballView, "y", footballView.getY(), footballView.getY() + movement);
        ObjectAnimator animX = ObjectAnimator.ofFloat(footballView, "x", footballView.getX(),footballView.getX() + xMovement, footballView.getX());
        animX.setDuration(1000);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                activateGoalView(goalSide);
                playGoalSound();
                bringBackBall(movement);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.setDuration(1000);
        anim.start();
        animX.start();
        //anim.reverse();

    }

    private void activateGoalView(GoalSide goalSide) {
        if (goalSide.equals(GoalSide.PlayerOneSide)) {
            ImageView goalView = (ImageView) findViewById(R.id.player_one_goal);
            ObjectAnimator anim = ObjectAnimator.ofFloat(goalView, "alpha", 1, 0, 1, 0);
            anim.setDuration(100);
            anim.start();
        } else {
            ImageView goalView = (ImageView) findViewById(R.id.player_two_goal);
            ObjectAnimator anim = ObjectAnimator.ofFloat(goalView, "alpha", 1, 0, 1, 0);
            anim.setDuration(100);
            anim.start();
        }
    }

    private void bringBackBall(int movement) {
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(footballView, "y", footballView.getY(), footballView.getY() - movement);
        ;
        anim2.setDuration(1);
        anim2.start();
        anim2.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!gameOver) {
                    setFormula();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public void playerOneAnswerSubmitted(View view) {
        if (!lock) {
            if (true == firstMap.get(view.getId())) {
                lock = true;
                playerOneScore += 1;
                TextView playerOneView = (TextView) findViewById(R.id.player_one_name);
                playerOneView.setText(String.valueOf(playerOneScore));
                throwBall(GoalSide.PlayerTwoSide);
                InitializeMappingMap();
            } else {
                playerOneScore -= 1;
                TextView playerOneView = (TextView) findViewById(R.id.player_one_name);
                playerOneView.setText(String.valueOf(playerOneScore));
                activateGoalView(GoalSide.PlayerOneSide);
            }
        }
    }

    public void playerTwoAnswerSubmitted(View view) throws InterruptedException {
        if (!lock) {
            if (true == secondMap.get(view.getId())) {
                lock = true;
                playerTwoScore += 1;
                TextView playerTwoView = (TextView) findViewById(R.id.player_two_name);
                playerTwoView.setText(String.valueOf(playerTwoScore));
                throwBall(GoalSide.PlayerOneSide);
                InitializeMappingMap();
            } else {
                playerTwoScore -= 1;
                TextView playerTwoView = (TextView) findViewById(R.id.player_two_name);
                playerTwoView.setText(String.valueOf(playerTwoScore));
                activateGoalView(GoalSide.PlayerTwoSide);
            }

        }
    }

    public void continuePressed(View view) {
        if (mState == MusicPlayerState.Paused) {
            mp.start();
        }
        startCountdown(timeLeft, tickTime);
        RelativeLayout backButtonPressedLayout = (RelativeLayout) findViewById(R.id.backPressedLayout);
        backButtonPressedLayout.setVisibility(View.GONE);
    }

    public void restartButton(View view) {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void navigateToMainScreen(View view) {
        super.onBackPressed();
    }
}