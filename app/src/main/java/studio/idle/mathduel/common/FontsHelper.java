package studio.idle.mathduel.common;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Udit on 30/03/15.
 */
public class FontsHelper {

    public static void setDigitalFont( Activity activity, TextView... views ) {
        Typeface custom_font = Typeface.createFromAsset(activity.getResources().getAssets(), "fonts/digital_font.ttf");
        for(TextView view : views) {
            view.setTypeface(custom_font);
        }
    }
}
