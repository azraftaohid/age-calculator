package com.coolninja.agecalculator.utilities;

@SuppressWarnings("unused")
public enum Error {
    DEFAULT(-1),
    NOT_FOUND(420);

    private int mCode;

    Error(int code) {
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }
}
