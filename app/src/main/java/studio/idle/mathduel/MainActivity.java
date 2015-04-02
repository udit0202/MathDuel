package studio.idle.mathduel;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import studio.idle.mathduel.common.CommonConstants;
import studio.idle.mathduel.common.PreferenceKeys;


public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener{

    private boolean isSoundOn, selectLevelViewVisible;
    private int gameLevel = 0; // default Easy
    private int gameDuration = 180000; //default 3 minutes
    private RelativeLayout selectLevelLayout, mainScreenLayout;
    private RadioGroup selectLevelRadioGroup, gameDurationRadioGroup;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Hide the action bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        selectLevelRadioGroup = (RadioGroup) findViewById(R.id.selectLevelRadioGroup);
        gameDurationRadioGroup = (RadioGroup) findViewById(R.id.gameDurationRadioGroup);
        selectLevelLayout = (RelativeLayout) findViewById(R.id.selectLevelLayout);
        mainScreenLayout = (RelativeLayout) findViewById(R.id.mainScreenLayout);

        setRadioGroupListeners();
    }

    private void setRadioGroupListeners() {
        gameDurationRadioGroup.setOnCheckedChangeListener(this);
        selectLevelRadioGroup.setOnCheckedChangeListener(this);
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

    @Override
    public void onBackPressed() {
        if(selectLevelViewVisible) {
            mainScreenLayout.setVisibility(View.VISIBLE);
            selectLevelLayout.setVisibility(View.GONE);
            selectLevelViewVisible = false;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        decideOnSoundButton();
    }

    @Override
    public void onResume() {
        Log.i("Checking", "onResume");
        super.onResume();
    }

    @Override
    public void onRestart() {
        Log.i("Checking", "onRestart");
        super.onResume();
    }

    public void openSelectLevelLayout(View view) {
        mainScreenLayout.setVisibility(View.GONE);
        selectLevelLayout.setVisibility(View.VISIBLE);
        selectLevelLayout.setClickable(true);
        //Alpha Animation
        ObjectAnimator anim = ObjectAnimator.ofFloat(selectLevelLayout, "alpha", 0f, 1f);
        anim.setDuration(1000);
        anim.start();
        selectLevelViewVisible = true;
    }

    @Override
    protected void onPause() {
        Log.i("Checking", "MainActivity onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i("Checking", "MainActivity onStop()");
        // Temporarily Putting it here
        mainScreenLayout.setVisibility(View.VISIBLE);
        selectLevelLayout.setVisibility(View.GONE);
        selectLevelViewVisible = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i("Checking", "MainActivity onDestroy()");
        super.onDestroy();
    }

    public void toggleSound(View view) {
        settings = getSharedPreferences(CommonConstants.PREF_FILE_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        isSoundOn = !isSoundOn; //toggle Sound
        editor.putBoolean(PreferenceKeys.isSoundOnKey, isSoundOn);
        editor.commit();

        //Logic to change later
        setSoundButton();
    }

    public void startGameActivity(View view) throws InterruptedException {
        EditText playerOneEditText = (EditText) findViewById(R.id.playerOneNameInput);
        EditText playerTwoEditText = (EditText) findViewById(R.id.playerTwoNameInput);

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(CommonConstants.GAME_LEVEL, gameLevel);
        intent.putExtra(CommonConstants.PLAYER_ONE_NAME, playerOneEditText.getText().toString());
        intent.putExtra(CommonConstants.PLAYER_TWO_NAME, playerTwoEditText.getText().toString());
        intent.putExtra(CommonConstants.GAME_DURATION, gameDuration);
        intent.putExtra(CommonConstants.IS_SOUND_ON, isSoundOn);
        //start Game Activity
        startActivity(intent);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(group.getId() == R.id.selectLevelRadioGroup) {
            selectLevelChanged(checkedId);
        } else if (group.getId() == R.id.gameDurationRadioGroup) {
            gameDurationChanged(checkedId);
        }

    }

    private void gameDurationChanged(int checkedId) {
        switch (checkedId) {
            case R.id.oneMinuteButton :
                gameDuration = 6000; // 1 Minute
                break;
            case R.id.threeMinuteButton:
                gameDuration = 180000; // 3 Minutes
                break;
            case R.id.fiveMinuteButton:
                gameDuration = 300000; // 5 Minutes
                break;
            case R.id.tenMinuteButton:
                gameDuration = 600000; // 10 Minutes
        }
    }

    private void selectLevelChanged(int checkedId) {
        switch (checkedId) {
            case R.id.easyRadioButton :
                gameLevel = 0;
                break;
            case R.id.mediumRadioButton:
                gameLevel = 1;
                break;
            case R.id.hardRadioButton:
                gameLevel = 2;
        }
    }
}
