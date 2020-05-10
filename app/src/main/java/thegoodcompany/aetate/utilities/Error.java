package thegoodcompany.aetate.utilities;

@SuppressWarnings("unused")
public enum Error {
    DEFAULT(-1, "Something went wrong"),
    NOT_FOUND(420, "We couldn't find the object specified");

    private int mCode;
    private String mMessage;

    Error(int code, String message) {
        mCode = code;
        mMessage = message;
    }

    public int getCode() {
        return mCode;
    }

    public String getMessage() {
        return mMessage;
    }
}
