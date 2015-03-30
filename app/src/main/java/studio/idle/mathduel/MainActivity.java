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

    private boolean isSoundOn, levelViewVisible;
    int gameLevel = 0, gameDuration = 180000;
    RelativeLayout selectLevelLayout, mainLayout;
    RadioGroup levelGroup, durationGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Hide the action bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        //Decide on Sound Button
        SharedPreferences settings = getSharedPreferences(CommonConstants.PREF_FILE_NAME, 0);
        isSoundOn = settings.getBoolean(PreferenceKeys.isSoundOnKey, true);
        setSoundButton();

        levelGroup = (RadioGroup) findViewById(R.id.radioGroupLevel);
        levelGroup.setOnCheckedChangeListener(this);
        durationGroup = (RadioGroup) findViewById(R.id.radioGroupDuration);
        durationGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onBackPressed() {
        if(levelViewVisible) {
            selectLevelLayout = (RelativeLayout) findViewById(R.id.selectLevelLayout);
            mainLayout = (RelativeLayout) findViewById(R.id.selectLevelLayout);
            mainLayout.setVisibility(View.VISIBLE);
            selectLevelLayout.setVisibility(View.GONE);
            levelViewVisible = false;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        Log.i("Udit", "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i("Udit", "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i("Udit", "onDestroy");
        super.onDestroy();
    }

    public void toggleSound(View view) {
        SharedPreferences settings = getSharedPreferences(CommonConstants.PREF_FILE_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        //Reading value from shared preferences
        isSoundOn = settings.getBoolean(PreferenceKeys.isSoundOnKey, true);
        isSoundOn = !isSoundOn; //toggle Sound
        editor.putBoolean(PreferenceKeys.isSoundOnKey, isSoundOn);
        editor.commit();

        //Logic to change later
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

    public void selectLevel(View view) {
        selectLevelLayout = (RelativeLayout) findViewById(R.id.selectLevelLayout);
        mainLayout = (RelativeLayout) findViewById(R.id.selectLevelLayout);
        mainLayout.setVisibility(View.GONE);
        selectLevelLayout.setVisibility(View.VISIBLE);
        selectLevelLayout.setClickable(true);

        //Alpha Animation
        ObjectAnimator anim = ObjectAnimator.ofFloat(selectLevelLayout, "alpha", 0f, 1f);
        anim.setDuration(500);
        anim.start();
        levelViewVisible = true;
    }

    public void startGameActivity(View view) {
        EditText playerOneEditText = (EditText) findViewById(R.id.playerOne);
        EditText playerTwoEditText = (EditText) findViewById(R.id.playerTwo);

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(CommonConstants.GAME_LEVEL, gameLevel);
        intent.putExtra(CommonConstants.PLAYER_ONE, playerOneEditText.getText().toString());
        intent.putExtra(CommonConstants.PLAYER_TWO, playerTwoEditText.getText().toString());
        intent.putExtra(CommonConstants.GAME_DURATION, gameDuration);

        startActivity(intent);
        mainLayout.setVisibility(View.VISIBLE);
        selectLevelLayout.setVisibility(View.GONE);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(group.getId() == R.id.radioGroupLevel) {
            levelRadioChanged(checkedId);
        } else if (group.getId() == R.id.radioGroupDuration) {
            durationRadioChanged(checkedId);
        }

    }

    private void durationRadioChanged(int checkedId) {
        switch (checkedId) {
            case R.id.oneMinuteButton :
                gameDuration = 60000; // 1 Minute
                break;
            case R.id.threeMinuteButton:
                gameDuration = 180000; // 3 Minutes
                break;
            case R.id.fiveMinuteButton:
                gameDuration = 300000; // 5 Minutes
                break;
            case R.id.tenMinuteButton:
                gameDuration = 600000; // 10 Minutes
                break;
        }
    }

    private void levelRadioChanged(int checkedId) {

        switch (checkedId) {
            case R.id.easyRadioButton :
                gameLevel = 0;
                break;
            case R.id.mediumRadioButton:
                gameLevel = 1;
                break;
            case R.id.hardRadioButton:
                gameLevel = 2;
                break;
        }
    }
}
