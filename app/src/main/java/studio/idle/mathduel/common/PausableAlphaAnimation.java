package studio.idle.mathduel.common;

import android.view.animation.AlphaAnimation;
import android.view.animation.Transformation;

/**
 * Created by Udit on 01/04/15.
 */
public class PausableAlphaAnimation extends AlphaAnimation {

    private long mElapsedAtPause = 0;
    private boolean mPaused = false;

    public PausableAlphaAnimation(float fromAlpha, float toAlpha) {
        super(fromAlpha, toAlpha);
    }

    @Override
    public boolean getTransformation(long currentTime, Transformation outTransformation) {
        if(mPaused && mElapsedAtPause == 0) {
            mElapsedAtPause = currentTime - getStartTime();
        }
        if(mPaused)
            setStartTime(currentTime - mElapsedAtPause);
        return super.getTransformation(currentTime, outTransformation);
    }

    public void pause() {
        mElapsedAtPause = 0;
        mPaused = true;
    }

    public void resume() {
        mPaused = false;
    }
}
