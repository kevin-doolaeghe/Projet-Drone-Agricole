package fr.rostand.drone.model;

import android.graphics.Bitmap;

public class MissionImage {
    private Bitmap mImage;
    private long mPosition;

    public MissionImage(Bitmap image, long position) {
        mImage = image;
        mPosition = position;
    }

    public Bitmap getImage() {
        return mImage;
    }

    public void setImage(Bitmap mImage) {
        this.mImage = mImage;
    }

    public long getPosition() {
        return mPosition;
    }

    public void setPosition(long mPosition) {
        this.mPosition = mPosition;
    }
}
