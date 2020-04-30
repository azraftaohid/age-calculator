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
import java.util.Calendar;

import static com.coolninja.agecalculator.ui.MainActivity.LOG_D;
import static com.coolninja.agecalculator.ui.MainActivity.LOG_I;
import static com.coolninja.agecalculator.ui.MainActivity.LOG_V;

//TODO Figure out a way to reduce the time takes to load a avatar; or do it on another thread
public class Avatar {
    private static final String LOG_TAG = Avatar.class.getSimpleName();
    private static final String LOG_TAG_PERFORMANCE = LOG_TAG + ".performance";

    private static final String SEPARATOR = "-";
    private static final String FORMAT_FILE_NAME = "%s" + SEPARATOR + "%s";
    private static final String CACHE = "cached";
    private static final String STORE = "stored";

    private Context mContext;
    private Bitmap mAvatarBitmap;
    private String mAvatarFileName;

    private Avatar(Context context) {
        mContext = context;
    }

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

        String fileName = String.format(FORMAT_FILE_NAME, CACHE, generatePngFileName(mContext.getCacheDir()));

        if (LOG_V) Log.v(LOG_TAG, "Caching avatar image");
        File avatarFile = new File(mContext.getCacheDir(), fileName);

        try (FileOutputStream outputStream = new FileOutputStream(avatarFile)) {
            mAvatarBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            mAvatarFileName = fileName;

            if (LOG_V) Log.v(LOG_TAG, "Avatar image compressed successfully");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Couldn't compress avatar image to the output stream: " + (e.getMessage() != null ? e.getMessage() : ""));
            e.printStackTrace();
        }

    }

    public void storePermanently() {
        Calendar start;
        if (LOG_D) start = Calendar.getInstance();

        if (LOG_V) Log.v(LOG_TAG, "Storing avatar image permanently");

        if (!mAvatarFileName.split(SEPARATOR)[0].equals(STORE)) {
            String fileName = String.format(FORMAT_FILE_NAME, STORE, generatePngFileName(mContext.getFilesDir()));
            File avatarFile = new File(mContext.getFilesDir(), fileName);

            try (FileOutputStream outputStream = new FileOutputStream(avatarFile)) {
                if (LOG_V) Log.v(LOG_TAG, "Compressing avatar image");
                mAvatarBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                mAvatarFileName = fileName;

                if (LOG_D) Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) +
                        " milliseconds to permanently store avatar file");

                return;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Couldn't compress avatar image to the output stream: " + (e.getMessage() != null ? e.getMessage() : ""));
                e.printStackTrace();
            }

            return;
        }

        if (LOG_I) Log.i(LOG_TAG, "Avatar was already stored");
    }

    private void loadAvatarBitmap() {
        if (LOG_V) Log.v(LOG_TAG, "Loading avatar image from storage");

        assert mAvatarFileName != null : "Avatar file name must be defined before invoking Avatar#loadAvatarBitmap()";
        File avatarFile;
        avatarFile = new File(mAvatarFileName.split(SEPARATOR)[0].equals(STORE) ? mContext.getFilesDir() : mContext.getCacheDir(), mAvatarFileName);

        if (avatarFile.exists()) {
            if (LOG_I) Log.i(LOG_TAG, "Avatar image exists");

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
            Log.e(LOG_TAG, "Couldn't load avatar image, because no such file exists");
        }

    }

    private String generatePngFileName(File dir) {
        if (LOG_V) Log.v(LOG_TAG, "Generating png file name");

        String uniqueName;
        int rand;

        do {
            rand = (int) (Math.random() * 100000);
            uniqueName = rand + ".png";
        } while (!isUniqueFileName(uniqueName, dir));

        return uniqueName;
    }

    private boolean isUniqueFileName(String name, File dir) {
        boolean isMatched = false;

        String[] existingNames = dir.list();
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

    public RoundedBitmapDrawable getCircularDrawable() {
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), mAvatarBitmap);
        drawable.setCircular(true);

        return drawable;
    }

    public String getAvatarFileName() {
        return mAvatarFileName;
    }

    public boolean deleteAvatarFile() {
        Calendar start;
        if (LOG_D) start = Calendar.getInstance();

        if (LOG_V) Log.v(LOG_TAG, "Attempting to delete avatar image file");

        if (mAvatarFileName != null) {
            File file = new File(mContext.getFilesDir(), mAvatarFileName);
            boolean isDeleted = file.delete();

            if (LOG_D) Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) +
                    " milliseconds to delete avatar image file");

            return isDeleted;
        }

        if (LOG_I) Log.i(LOG_TAG, "Avatar image file wasn't stored permanently before");
        return false;
    }

}
