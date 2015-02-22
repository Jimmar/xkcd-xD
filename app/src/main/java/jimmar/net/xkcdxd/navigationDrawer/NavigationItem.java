package jimmar.net.xkcdxd.navigationDrawer;

import android.graphics.drawable.Drawable;

/**
 * Created by poliveira on 24/10/2014.
 */
public class NavigationItem {
    private String mText;
    private Drawable mDrawable;

    public NavigationItem(String text, Drawable drawable) {
        mText = text;
        mDrawable = drawable;
    }

    public NavigationItem(String text) {
        new NavigationItem(text, null);
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
    }
}
