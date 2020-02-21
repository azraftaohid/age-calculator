package com.coolninja.agecalculator;

import android.content.Intent;

public interface BirthDayPicker {
    void onBirthdayPick(int requestCode, int resultCode, Intent data);
}