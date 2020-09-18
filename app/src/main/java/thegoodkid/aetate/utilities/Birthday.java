package thegoodkid.aetate.utilities;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Birthday)) return false;
        Birthday birthday = (Birthday) o;
        return mMonth == birthday.mMonth &&
                mDay == birthday.mDay &&
                mYear == birthday.mYear;
    }

    public boolean equals(int year, int month, int day) {
        return mMonth == month &&
                mDay == day &&
                mYear == year;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mMonth, mDay, mYear);
    }
}
