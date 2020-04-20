package com.coolninja.agecalculator.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.util.Log;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.coolninja.agecalculator.ui.MainActivity.LOG_D;
import static com.coolninja.agecalculator.ui.MainActivity.LOG_I;
import static com.coolninja.agecalculator.ui.MainActivity.LOG_V;

//TODO Figure out a way to reduce the time takes to load a avatar; or do it on another thread
public class Avatar {
    private static final String LOG_TAG = Avatar.class.getSimpleName();

    private Context mContext;
    private Bitmap mAvatarBitmap;
    private String mAvatarFileName;

    private Avatar(Context context) {
        mContext = context;
    }

    @SuppressWarnings("WeakerAccess")
    public Avatar(Context context, Bitmap avatarImage) {
        if (LOG_V) Log.v(LOG_TAG, "Creating avatar from bitmap");

        mContext = context;
        mAvatarBitmap = avatarImage;

        prepare();
    }

    public static Avatar retrieveAvatar(Context context, String avatarFileName) {
        Avatar avatar = new Avatar(context);
        avatar.mAvatarFileName = avatarFileName;
        avatar.loadAvatarBitmap();

        return avatar;
    }

    public static Avatar makeCopy(Avatar avatar) {
        Avatar newAvatar = new Avatar(avatar.mContext);
        newAvatar.mAvatarFileName = avatar.getAvatarFileName();
        newAvatar.loadAvatarBitmap();

        return newAvatar;
    }

    private void prepare() {
        if (LOG_V) Log.v(LOG_TAG, "Preparing avatar for use");

        int height = mAvatarBitmap.getHeight();
        int width = mAvatarBitmap.getWidth();
        if (height != width) {
            if (LOG_D) Log.d(LOG_TAG, "Received avatar image wasn't square");

            int minLength;
            int x;
            int y;
            if (height < width) {
                minLength = height;
                x = (width - height) / 2;
                y = 0;
            } else {
                minLength = width;
                x = 0;
                y = (height - width) / 2;
            }

            mAvatarBitmap = Bitmap.createBitmap(mAvatarBitmap, x, y, minLength, minLength);
        } else if (LOG_D) {
            Log.d(LOG_TAG, "Received avatar image was square");
        }

        mAvatarFileName = generatePngFileName();

        if (LOG_V) Log.v(LOG_TAG, "Creating avatar file");
        File avatarFile = new File(mContext.getFilesDir(), mAvatarFileName);

        try (FileOutputStream outputStream = new FileOutputStream(avatarFile)){
            if (LOG_V) Log.v(LOG_TAG, "Compressing the avatar bitmap to the output stream");
            mAvatarBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadAvatarBitmap() {
        if (LOG_V) Log.v(LOG_TAG, "Loading avatar bitmap from file");

        assert mAvatarFileName != null : "Avatar file name must be defined before invoking load avatar bitmap";
        File avatarFile = new File(mContext.getFilesDir(), mAvatarFileName);

        if (avatarFile.exists()) {
            if (LOG_I) Log.i(LOG_TAG, "Avatar file with the file name exists");

            try (FileInputStream inputStream = new FileInputStream(avatarFile)){
                if (Build.VERSION.SDK_INT >= 28) {
                    if (LOG_I) Log.i(LOG_TAG, "Build SDK version is higher than 27");
                    if (LOG_V) Log.v(LOG_TAG, "Decoding the file into bitmap using image decoder");

                    ImageDecoder.Source source = ImageDecoder.createSource(avatarFile);
                    mAvatarBitmap = ImageDecoder.decodeBitmap(source);
                } else {
                    if (LOG_I) Log.i(LOG_TAG, "Build SDK version is lower than 28");
                    if (LOG_V) Log.v(LOG_TAG, "Decoding the stream into bitmap using bitmap factory");

                    mAvatarBitmap = BitmapFactory.decodeStream(inputStream);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(LOG_TAG, "Couldn't load avatar bitmap, because no such file exists");
        }

    }

    private String generatePngFileName() {
        if (LOG_V) Log.v(LOG_TAG, "Generating png file name");

        String uniqueName;
        int rand;

        do {
            rand = (int) (Math.random() * 100000);
            uniqueName = rand + ".png";
        } while (!isUniqueFileName(uniqueName));

        return uniqueName;
    }

    private boolean isUniqueFileName(String name) {
        boolean isMatched = false;

        String[] existingNames = mContext.getFilesDir().list();
        if (LOG_V) Log.v(LOG_TAG, "Existing file names: " + Arrays.toString(existingNames));
        if (LOG_V) Log.v(LOG_TAG, "Passed file name: " + name);

        if (existingNames != null) {
            for (String existingName : existingNames) {
                if (existingName.equalsIgnoreCase(name)) {
                    if (LOG_V) Log.v(LOG_TAG, "Unique file name found");
                    isMatched = true;
                    break;
                }
            }

        }

        return !isMatched;
    }

    public Bitmap getBitmap() {
        return mAvatarBitmap;
    }

    @SuppressWarnings("WeakerAccess")
    public RoundedBitmapDrawable getCircularDrawable() {
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), mAvatarBitmap);
        drawable.setCircular(true);

        return drawable;
    }

    public String getAvatarFileName() {
        return mAvatarFileName;
    }

    @SuppressWarnings("WeakerAccess")
    public boolean deleteAvatarFile() {
        if (LOG_V) Log.v(LOG_TAG, "Deleting avatar file");

        File file = new File(mContext.getFilesDir(), mAvatarFileName);
        return file.delete();
    }

}
