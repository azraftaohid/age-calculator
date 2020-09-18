package thegoodkid.aetate.utilities;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public enum Error {
    DEFAULT(-1, "Default error", "Something went wrong"),

    INCONSISTENT_PROFILE(11000, "Profile in illegal state", "Profile maybe corrupted"),
    INCONSISTENT_TAGGED_ABSENT(11001, "Found tag on profile that does not exist", "Profile maybe corrupted"),
    INCONSISTENT_MODIFIED_ABSENT(11002, "Modified profile that does not exist", "Profile maybe corrupted"),
    INCONSISTENT_PINNED_ABSENT(11003, "Pinned profile that does not exist", "Profile maybe corrupted"),

    NOT_FOUND(420, "Unknown object", "We couldn't find the object specified");

    private int mCode;
    private String mDevMessage;
    private String mClientMessage;

    Error(int code, String devMessage, String clientMessage) {
        mCode = code;
        mDevMessage = devMessage;
        mClientMessage = clientMessage;
    }

    @NonNull
    public static String createDevMessage(@NonNull Error error) {
        return "Error " + error.mCode + ": " + error.mDevMessage;
    }

    public int getCode() {
        return mCode;
    }

    public String getMessage() {
        return mClientMessage;
    }
}
