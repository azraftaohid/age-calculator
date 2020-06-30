package thegoodcompany.aetate.utilities;

public class Birthday {
    private int mMonth;
    private int mDay;
    private int mYear;

    public Birthday(int birthYear, int birthMonth, int birthDay) {
        mMonth = birthMonth;
        mDay = birthDay;
        mYear = birthYear;
    }

    public int getMonthValue() {
        return mMonth;
    }

    public int getDayOfMonth() {
        return mDay;
    }

    public Birthday setDay(int dayOfMonth) {
        mDay = dayOfMonth;

        return this;
    }

    public int getYear() {
        return mYear;
    }

    public Birthday setYear(int year) {
        mYear = year;

        return this;
    }

    public Month getMonth() {
        return Month.values()[mMonth];
    }

    public Birthday setMonth(int monthIntValue) {
        mMonth = monthIntValue;

        return this;
    }
}
