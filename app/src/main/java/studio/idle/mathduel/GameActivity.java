package studio.idle.mathduel;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import studio.idle.mathduel.common.CommonConstants;
import studio.idle.mathduel.common.FontsHelper;
import studio.idle.mathduel.common.Formula;
import studio.idle.mathduel.common.FormulaHelper;
import studio.idle.mathduel.common.OptionsHelper;
import studio.idle.mathduel.common.PausableAlphaAnimation;
import studio.idle.mathduel.common.PreferenceKeys;
import studio.idle.mathduel.common.StringUtils;
import studio.idle.mathduel.common.WrongOptionsObject;


public class GameActivity extends Activity {

    int gameLevel = 1, gameDuration = 180000, tickTime = 1000, timeLeft = gameDuration; //setting default values
    int answer, playerOneScore = 0, playerTwoScore = 0;
    boolean lock, gameOver, isSoundOn, startingAnimationOn, countDownStarted, backLayoutVisible, countDownCouldNotBegin, manuallyPaused;
    String playerOneName, playerTwoName;
    Random rand;

    RelativeLayout vsOverlayView, mainGameView, backButtonPressedLayout, gameOverLayout;
    ImageView footballView;
    TextView playerOneTimer, playerTwoTimer, playerOneScoreView, playerTwoScoreView, gameOverTextView;
    //BackButtonPressed views
    TextView timeLeftBackView, playerOneScoreBackView, playerTwoScoreBackView, playerOneNameView, playerTwoNameView;
    TextView playerOneGameOverScoreView, playerTwoGameOverScoreView;
    MediaPlayer mp;
    CountDownTimer timer;
    Display display;
    CommonConstants.MusicPlayerState mState;

    Map<Integer, Boolean> firstIdToBooleanMap = new HashMap<>();
    Map<Integer, Boolean> secondIdToBooleanMap = new HashMap<>();
    Map<Integer, Integer> firstIndexToIdMap = new HashMap<>();
    Map<Integer, Integer> secondIndexToIdMap = new HashMap<>();
    Button formulaViewPlayer1, formulaViewPlayer2; //For now its a button
    Button wrongOptionPlayer1View1, wrongOptionPlayer1View2, wrongOptionPlayer2View1, wrongOptionPlayer2View2;
    Button gameOverExitButton, gameOverPlayAgainButton;
    ImageView ballHitGoalView;
    PausableAlphaAnimation alphaAnimationFadeOut, alphaAnimationFadeIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Hide the action bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_game);

        //Getting bundle values from parent activity
        readBundleValues();

        //Set Players Name In Match Starting
        setPlayersName();

        decideOnSoundButton();

        playerOneTimer = (TextView) findViewById(R.id.countDownTimerPlayer1);
        playerTwoTimer = (TextView) findViewById(R.id.countDownTimerPlayer2);
        playerOneTimer.setText(getTimerText(gameDuration));
        playerTwoTimer.setText(getTimerText(gameDuration));

        playerOneScoreView = (TextView) findViewById(R.id.playerOneScore);
        playerTwoScoreView = (TextView) findViewById(R.id.playerTwoScore);
        playerOneScoreBackView = (TextView) findViewById(R.id.playerOneScoreBoard);
        playerTwoScoreBackView = (TextView) findViewById(R.id.playerTwoScoreBoard);
        timeLeftBackView = (TextView) findViewById(R.id.timeLeft);
        playerOneGameOverScoreView = (TextView) findViewById(R.id.playerOneScoreGameOver);
        playerTwoGameOverScoreView = (TextView) findViewById(R.id.playerTwoScoreGameOver);

        FontsHelper.setDigitalFont(this, playerOneTimer, playerTwoTimer, playerOneScoreView, playerTwoScoreView, playerOneScoreBackView,
                playerTwoScoreBackView, timeLeftBackView, playerOneGameOverScoreView, playerTwoGameOverScoreView);

        formulaViewPlayer1 = (Button) findViewById(R.id.questionViewPlayerOne);
        formulaViewPlayer2 = (Button) findViewById(R.id.questionViewPlayerTwo);

        //Start playing music
        playIntroMusic();

        showStartingAnimation();
    }

    private void decideOnSoundButton() {
        SharedPreferences settings = getSharedPreferences(CommonConstants.PREF_FILE_NAME, 0);
        isSoundOn = settings.getBoolean(PreferenceKeys.isSoundOnKey, true);
        setSoundButton();
    }

    private void setSoundButton() {
        Button soundButton = (Button) findViewById(R.id.soundButton);
        if (isSoundOn) {
            soundButton.setAlpha(1f);
        } else {
            soundButton.setAlpha(0.5f);
        }
    }

    public void toggleSound(View view) {
        SharedPreferences settings = getSharedPreferences(CommonConstants.PREF_FILE_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        isSoundOn = !isSoundOn; //toggle Sound
        editor.putBoolean(PreferenceKeys.isSoundOnKey, isSoundOn);
        editor.commit();

        //Logic to change later
        setSoundButton();
    }

    @Override
    protected void onPause() {
        Log.i("Checking", "game onPause()");
        if (startingAnimationOn) {
            Log.i("Checking", "animation paused");
            alphaAnimationFadeIn.pause();
            alphaAnimationFadeOut.pause();
        }

        if (timer != null) {
            timer.cancel(); // Stop the timer, will be started
        }
        if (mState == CommonConstants.MusicPlayerState.Playing) {
            mp.pause();
            mState = CommonConstants.MusicPlayerState.Paused;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mState == CommonConstants.MusicPlayerState.Paused && !manuallyPaused) {
            mp.start();
            mState = CommonConstants.MusicPlayerState.Playing;
        }
        if (timeLeft > 0 && timeLeft < gameDuration && !manuallyPaused) {
            Log.i("Time left", timeLeft + "");
            startCountdown(timeLeft, tickTime);
        }
        if (startingAnimationOn && alphaAnimationFadeIn.isPaused() && !manuallyPaused) {
            Log.i("Checking", "starting Animation resumed");
            alphaAnimationFadeIn.resume();
            alphaAnimationFadeOut.resume();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.i("Checking", "game onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i("Checking", "onDestroy()");
        super.onDestroy();
    }

    private void showStartingAnimation() {
        vsOverlayView = (RelativeLayout) findViewById(R.id.vsOverlayView);
        mainGameView = (RelativeLayout) findViewById(R.id.mainScreenLayout);

        alphaAnimationFadeOut = new PausableAlphaAnimation(0f, 1f);
        alphaAnimationFadeIn = new PausableAlphaAnimation(1f, 0f);
        alphaAnimationFadeOut.setDuration(5000);
        alphaAnimationFadeIn.setDuration(5000);
        alphaAnimationFadeIn.setFillAfter(true);
        mainGameView.setAnimation(alphaAnimationFadeOut);
        vsOverlayView.setAnimation(alphaAnimationFadeIn);
        vsOverlayView.setClickable(true);

        alphaAnimationFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.i("Checking", "Animation Ended");
                playWhistleSound();
                startingAnimationOn = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        startingAnimationOn = true;
        alphaAnimationFadeOut.start();
        alphaAnimationFadeIn.start();
    }

    private void playWhistleSound() {
        mp = MediaPlayer.create(GameActivity.this, R.raw.whistle);
        setMediaPlayerVolume(mp);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.release();
                if (!gameOver && !backLayoutVisible) { // Handling the scenario in which user pressed back button while start whistle
                    startCountdown(gameDuration, tickTime);
                    setFormula();
                    vsOverlayView.setClickable(false);
                    mainGameView.setClickable(true);
                } else {
                    //Countdown stopped because of stopped button
                    countDownCouldNotBegin = true;
                }
            }
        });
        mp.start();
    }

    private void setMediaPlayerVolume(MediaPlayer mp) {
        if (isSoundOn) {
            mp.setVolume(1, 1);
        } else {
            mp.setVolume(0, 0);
        }
    }

    private void setPlayersName() {
        TextView playerOneVsView = (TextView) findViewById(R.id.playerOneNameVsView);
        TextView playerTwoVsView = (TextView) findViewById(R.id.playerTwoNameVsView);
        TextView playerOneNameScore = (TextView) findViewById(R.id.player_one_name);
        TextView playerTwoNameScore = (TextView) findViewById(R.id.player_two_name);
        if (playerOneName.equals("")) {
            playerOneName = "Player 1";
        }
        playerOneVsView.setText(playerOneName);
        playerOneNameScore.setText(playerOneName);

        if (playerTwoName.equals("")) {
            playerTwoName = "Player 2";
        }
        playerTwoVsView.setText(playerTwoName);
        playerTwoNameScore.setText(playerTwoName);

    }

    private void readBundleValues() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            gameLevel = extras.getInt(CommonConstants.GAME_LEVEL);
            playerOneName = extras.getString(CommonConstants.PLAYER_ONE_NAME);
            playerTwoName = extras.getString(CommonConstants.PLAYER_TWO_NAME);
            gameDuration = extras.getInt(CommonConstants.GAME_DURATION);
            timeLeft = gameDuration;
            isSoundOn = extras.getBoolean(CommonConstants.IS_SOUND_ON);
        }
    }

    private void startCountdown(int totalTime, int tickTime) {
        countDownStarted = true;
        timer = new CountDownTimer(totalTime, tickTime) {

            public void onTick(long millisUntilFinished) {
                timeLeft = (int) millisUntilFinished;
                playerOneTimer.setText(getTimerText(millisUntilFinished));
                playerTwoTimer.setText(getTimerText(millisUntilFinished));
            }

            public void onFinish() {
                playerOneTimer.setText("00:00");
                playerTwoTimer.setText("00:00");
                timeLeft = 0;
                gameOver();
            }
        }.start();
    }

    private String getTimerText(long millisUntilFinished) {
        long minutesRemaining = (millisUntilFinished / 1000) / 60;
        long secondsRemaining = (millisUntilFinished / 1000) % 60;
        String minutesString = StringUtils.convertToTimeFormat(minutesRemaining);
        String secondStrings = StringUtils.convertToTimeFormat(secondsRemaining);
        return minutesString + " : " + secondStrings;
    }

    private void gameOver() {
        gameOver = true;
        mainGameView.setClickable(false);
        playWhistleSound(); //To convey end

        gameOverTextView = (TextView) findViewById(R.id.gameOverString);
        gameOverTextView.setVisibility(View.VISIBLE);

        gameOverExitButton = (Button) findViewById(R.id.gameOverMainMenuButton);
        gameOverPlayAgainButton = (Button) findViewById(R.id.playAgainButton);
        gameOverExitButton.setEnabled(false);
        gameOverPlayAgainButton.setEnabled(false);
        gameOverLayout = (RelativeLayout) findViewById(R.id.gameOverLayout);
        gameOverLayout.setVisibility(View.VISIBLE);
        gameOverLayout.setClickable(true);

        ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(gameOverLayout, "alpha", 0, 1);
        alphaAnimation.setDuration(1000);
        alphaAnimation.setStartDelay(2000);
        alphaAnimation.start();
        alphaAnimation.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                gameOverExitButton.setEnabled(true);
                gameOverPlayAgainButton.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        TextView winnerView = (TextView) findViewById(R.id.winnerHeading);

        if (playerOneScore > playerTwoScore) {
            winnerView.setText(playerOneName + " WON");
        } else if (playerOneScore < playerTwoScore) {
            winnerView.setText(playerTwoName + " WON");
        } else {
            winnerView.setText("Match Drawn");
        }
        playerOneGameOverScoreView.setText(String.valueOf(playerOneScore));
        playerTwoGameOverScoreView.setText(String.valueOf(playerTwoScore));
    }

    @Override
    public void onBackPressed() {
        Log.i("Checking", "onBackPressed()");
        if (!gameOver) {
            backLayoutVisible = true;
            manuallyPaused = true;
            if (startingAnimationOn) {
                alphaAnimationFadeIn.pause();
                alphaAnimationFadeOut.pause();
            }
            if (timer != null) {
                timer.cancel(); // Stop the timer, will be started
            }
            if (mState == CommonConstants.MusicPlayerState.Playing) {
                mp.pause();
                mState = CommonConstants.MusicPlayerState.Paused;
            }
            //Show the back Button Press Layout
            playerOneNameView = (TextView) findViewById(R.id.playerOneName);
            playerTwoNameView = (TextView) findViewById(R.id.playerTwoName);
            playerOneScoreBackView.setText(String.valueOf(playerOneScore));
            playerTwoScoreBackView.setText(String.valueOf(playerTwoScore));
            timeLeftBackView.setText(getTimerText(timeLeft));
            playerOneNameView.setText(playerOneName);
            playerTwoNameView.setText(playerTwoName);
            backButtonPressedLayout = (RelativeLayout) findViewById(R.id.backPressedLayout);
            backButtonPressedLayout.setVisibility(View.VISIBLE);
            backButtonPressedLayout.setClickable(true);
        }
    }

    private void playIntroMusic() {
        mp = MediaPlayer.create(GameActivity.this, R.raw.cheer);
        setMediaPlayerVolume(mp);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.release();
                mState = CommonConstants.MusicPlayerState.Stopped;
            }
        });
        mp.start();
        mState = CommonConstants.MusicPlayerState.Playing;
    }

    private void playGoalSound() {
        if (isSoundOn) {
            mp = MediaPlayer.create(GameActivity.this, R.raw.goal);
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            setMediaPlayerVolume(mp);
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
        }
    }

    public void setFormula() {
        // Initialising options map
        InitializeMappingMap();
        Formula formula = FormulaHelper.fetchFormula(gameLevel);
        showFormula(formula.firstOperand, formula.secondOperand, formula.operator);
        answer = FormulaHelper.calculateAnswer(formula.firstOperand, formula.secondOperand, formula.operator);
        showOptions(formula.operator);
        lock = false;
    }

    private void showOptions(String operand) {
        rand = new Random();
        Vector<Integer> vector = new Vector<>();
        //For 3 options
        vector.add(0);
        vector.add(1);
        vector.add(2);

        int correctBoxIndex = rand.nextInt(3); // For 0-2
        firstIdToBooleanMap.put(firstIndexToIdMap.get(correctBoxIndex), true);
        secondIdToBooleanMap.put(secondIndexToIdMap.get(correctBoxIndex), true);
        vector.remove(correctBoxIndex);

        int incorrectBoxIndex1 = vector.get(0); // remaining to show incorrect options
        int incorrectBoxIndex2 = vector.get(1);

        Button correctOptionPlayerOneView = (Button) findViewById(firstIndexToIdMap.get(correctBoxIndex)); //Button for now
        Button correctOptionPlayerTwoView = (Button) findViewById(secondIndexToIdMap.get(correctBoxIndex));
        correctOptionPlayerOneView.setText(String.valueOf(answer));
        correctOptionPlayerTwoView.setText(String.valueOf(answer));

        WrongOptionsObject wrongOptions = OptionsHelper.fetchWrongOptions(answer, gameLevel, operand);

        wrongOptionPlayer1View1 = (Button) findViewById(firstIndexToIdMap.get(incorrectBoxIndex1));
        wrongOptionPlayer2View1 = (Button) findViewById(secondIndexToIdMap.get(incorrectBoxIndex1));
        wrongOptionPlayer1View1.setText(String.valueOf(wrongOptions.getWrongOptionOne()));
        wrongOptionPlayer2View1.setText(String.valueOf(wrongOptions.getWrongOptionOne()));

        wrongOptionPlayer1View2 = (Button) findViewById(firstIndexToIdMap.get(incorrectBoxIndex2));
        wrongOptionPlayer2View2 = (Button) findViewById(secondIndexToIdMap.get(incorrectBoxIndex2));
        wrongOptionPlayer1View2.setText(String.valueOf(wrongOptions.getWrongOptionTwo()));
        wrongOptionPlayer2View2.setText(String.valueOf(wrongOptions.getWrongOptionTwo()));
    }

    private void InitializeMappingMap() {
        firstIndexToIdMap.put(0, R.id.optionOnePlayer1);
        firstIndexToIdMap.put(1, R.id.optionTwoPlayer1);
        firstIndexToIdMap.put(2, R.id.optionThreePlayer1);
        secondIndexToIdMap.put(0, R.id.optionOnePlayer2);
        secondIndexToIdMap.put(1, R.id.optionTwoPlayer2);
        secondIndexToIdMap.put(2, R.id.optionThreePlayer2);

        firstIdToBooleanMap.put(R.id.optionOnePlayer1, false);
        firstIdToBooleanMap.put(R.id.optionTwoPlayer1, false);
        firstIdToBooleanMap.put(R.id.optionThreePlayer1, false);
        secondIdToBooleanMap.put(R.id.optionOnePlayer2, false);
        secondIdToBooleanMap.put(R.id.optionTwoPlayer2, false);
        secondIdToBooleanMap.put(R.id.optionThreePlayer2, false);
    }

    private void showFormula(int firstOperand, int secondOperand, String operator) {
        String formulaString = firstOperand + " " + operator + " " + secondOperand;
        formulaViewPlayer1.setText(formulaString);
        formulaViewPlayer2.setText(formulaString);
    }

    public void throwBall(final CommonConstants.GoalSide goalSide) {
        footballView = (ImageView) findViewById(R.id.football);
        display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int height = size.y;
        final int width = size.x;
        final int yMovement = (goalSide == CommonConstants.GoalSide.PlayerTwoSide ? ((height / 2 - 50) * -1) : (height / 2) - 50);
        int xMovement = rand.nextInt(width / 10) - (width / 20);

        ObjectAnimator yAnimation = ObjectAnimator.ofFloat(footballView, "y", footballView.getY(), footballView.getY() + yMovement);
        ObjectAnimator xAnimation = ObjectAnimator.ofFloat(footballView, "x", footballView.getX(), footballView.getX() + xMovement, footballView.getX());
        yAnimation.setDuration(1000);
        xAnimation.setDuration(1000);
        yAnimation.start();
        xAnimation.start();

        yAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                activateGoalView(goalSide);
                playGoalSound();
                bringBackBall(yMovement);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    private void activateGoalView(CommonConstants.GoalSide goalSide) {
        if (goalSide.equals(CommonConstants.GoalSide.PlayerOneSide)) {
            ballHitGoalView = (ImageView) findViewById(R.id.player_one_goal_highlight);
        } else {
            ballHitGoalView = (ImageView) findViewById(R.id.player_two_goal_highlight);
        }
        ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(ballHitGoalView, "alpha", 1, 0, 1, 0);
        alphaAnimation.setDuration(100);
        alphaAnimation.start();
    }

    private void bringBackBall(int movement) {

        ObjectAnimator yReverseAnimation = ObjectAnimator.ofFloat(footballView, "y", footballView.getY(), footballView.getY() - movement);
        yReverseAnimation.setDuration(1);
        yReverseAnimation.start();
        yReverseAnimation.setStartDelay(1200); //1.2seconds
        yReverseAnimation.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!gameOver) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setFormula();
                        }
                    });
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
            if (true == firstIdToBooleanMap.get(view.getId())) {
                lock = true;
                playerOneScore += 1;
                playerOneScoreView.setText(String.valueOf(playerOneScore));
                throwBall(CommonConstants.GoalSide.PlayerTwoSide);
            } else {
                playerOneScore -= 1;
                playerOneScoreView.setText(String.valueOf(playerOneScore));
                activateGoalView(CommonConstants.GoalSide.PlayerOneSide);
            }
        }
    }

    public void playerTwoAnswerSubmitted(View view) throws InterruptedException {
        if (!lock) {
            if (true == secondIdToBooleanMap.get(view.getId())) {
                lock = true;
                playerTwoScore += 1;
                playerTwoScoreView.setText(String.valueOf(playerTwoScore));
                throwBall(CommonConstants.GoalSide.PlayerOneSide);
            } else {
                playerTwoScore -= 1;
                playerTwoScoreView.setText(String.valueOf(playerTwoScore));
                activateGoalView(CommonConstants.GoalSide.PlayerTwoSide);
            }
        }
    }

    public void continuePressed(View view) {
        manuallyPaused = false;
        if (startingAnimationOn) {
            alphaAnimationFadeIn.resume();
            alphaAnimationFadeOut.resume();
        }
        if (mState == CommonConstants.MusicPlayerState.Paused) {
            setMediaPlayerVolume(mp); //In case user toggled the sound
            mp.start();
            mState = CommonConstants.MusicPlayerState.Playing;
        }

        if (countDownStarted) {
            Log.i("Checking", "countDownStarted if loop");
            startCountdown(timeLeft, tickTime); //Basically resume
        } else if (countDownCouldNotBegin) {
            Log.i("Checking", "countDownCouldNotBegin");
            startCountdown(timeLeft, tickTime);
            countDownCouldNotBegin = false;
            vsOverlayView.setClickable(false);
            mainGameView.setClickable(true);
            setFormula();
        }
        backButtonPressedLayout = (RelativeLayout) findViewById(R.id.backPressedLayout);
        backButtonPressedLayout.setVisibility(View.GONE);
        backLayoutVisible = false;
    }

    public void restartGame(View view) {
        Log.i("Checking", "restartGame()");
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void navigateToMainScreen(View view) {
        super.onBackPressed();
    }
}