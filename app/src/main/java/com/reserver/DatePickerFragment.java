package com.reserver;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private static final int DEFAULT_YEAR = 2017;
    private static final int DEFAULT_MONTH = 0;
    private static final int DEFAULT_DAY = 1;
    private String[] nameOfMonth = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private String reservationDate;

    public String getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(String reservationDate) {
        this.reservationDate = reservationDate;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year, month, day;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Use the current date as the default date in the picker.
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        } else {
            year = DEFAULT_YEAR;
            month = DEFAULT_MONTH;
            day = DEFAULT_DAY;
        }

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthNumber, int dayOfMonth) {
        reservationDate = dayOfMonth + " " + nameOfMonth[monthNumber] + " " + year;
    }
}
