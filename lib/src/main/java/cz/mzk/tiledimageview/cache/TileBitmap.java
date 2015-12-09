package cz.mzk.tiledimageview.cache;

import android.graphics.Bitmap;

public class TileBitmap {
    private State state;
    private Bitmap bitmap;

    public TileBitmap(State state, Bitmap bitmap) {
        super();
        this.state = state;
        this.bitmap = bitmap;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public static enum State {
        IN_MEMORY, IN_DISK, NOT_FOUND;
    }
}
