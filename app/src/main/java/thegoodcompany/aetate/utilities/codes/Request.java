package thegoodcompany.aetate.utilities.codes;

public class Request {
    private static int activeRequestCode = 1100;
    public static final int REQUEST_DATE_OF_BIRTH = activeRequestCode++;
    public static final int REQUEST_NEW_PROFILE_INFO = activeRequestCode++;
    public static final int REQUEST_PICK_AVATAR = activeRequestCode++;
    public static final int REQUEST_CIRCULAR_CROP = activeRequestCode++;
    public static final int REQUEST_MODIFY_PROFILE_INFO = activeRequestCode++;
    public static final int REQUEST_FIRST_PROFILE_INFO = activeRequestCode++;
    public static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = activeRequestCode++;
    public static final int REQUEST_PICKING_AVATAR_PERMISSIONS = activeRequestCode++;
}
