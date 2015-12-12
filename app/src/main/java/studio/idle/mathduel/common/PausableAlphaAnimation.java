package studio.idle.mathduel.common;

import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Transformation;

/**
 * Created by Udit on 01/04/15.
 */
public class PausableAlphaAnimation extends AlphaAnimation {

    private long mElapsedAtPause = 0;
    private boolean mPaused = false;
    private boolean mResumed = false;
    private long currentTimeAtPause;
    public PausableAlphaAnimation(float fromAlpha, float toAlpha) {
        super(fromAlpha, toAlpha);
    }

    @Override
    public boolean getTransformation(long currentTime, Transformation outTransformation) {
        if(mPaused && mElapsedAtPause == 0) {
            mElapsedAtPause = currentTime - getStartTime();
            currentTimeAtPause = currentTime;
        }
        if(mPaused)
            setStartTime(currentTime - mElapsedAtPause);
        if(mResumed) {
            setStartTime(currentTime - mElapsedAtPause);
            mResumed = false;
        }
        Log.i("animation", currentTime +"");
        return super.getTransformation(currentTime, outTransformation);

    }

    public void pause() {
        mElapsedAtPause = 0;
        mPaused = true;
    }

    public void resume() {
        mPaused = false;
        mResumed = true;
    }

    public boolean isPaused() {
        return mPaused;
    }
}
