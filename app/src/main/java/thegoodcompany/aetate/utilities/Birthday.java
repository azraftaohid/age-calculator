package thegoodcompany.aetate.utilities;

public class Birthday {
    public static final int YEAR = 0;
    public static final int MONTH = 1;
    public static final int DAY = 2;

    private int[] mDateOfBirth = new int[3];

    public Birthday(int birthYear, int birthMonth, int birthDay) {
        mDateOfBirth[YEAR] = birthYear;
        mDateOfBirth[MONTH] = birthMonth;
        mDateOfBirth[DAY] = birthDay;
    }

    public int get(int field) {
        return mDateOfBirth[field];
    }

    public void set(int field, int value) {
        mDateOfBirth[field] = value;
    }

    public Month getMonth() {
        return Month.values()[get(MONTH)];
    }
}
