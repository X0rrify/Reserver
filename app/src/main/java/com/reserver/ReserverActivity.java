package com.reserver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

public class ReserverActivity extends AppCompatActivity implements CallReceiver.OnCallListener {

    private TextView labelGuestPhone;
    private EditText editTextGuestEmail;
    private NumberPicker pickerGuestsCount;
    private Button buttonDate;
    private Button buttonTime;
    private EditText editTextAdminEmail;
    private Button buttonSubmit;

    private DatePickerFragment dateDialog;
    private TimePickerFragment timeDialog;

    private final CallReceiver callReceiver = new CallReceiver();

    private DBProvider dbProvider;

    private static final int GUESTS_MIN_VALUE = 1;
    private static final int GUESTS_MAX_VALUE = 10;

    private static final int READ_PHONE_STATE_CODE = 0;

    private static final String ADMIN_EMAIL = "ADMIN_EMAIL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserver);
        initComponents();
        requestPermissions();
        callReceiver.setOnCallListener(this);
        dbProvider = new DBProvider(getApplicationContext());
        dbProvider.open();
        loadAdminEmail();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerCallReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(callReceiver);
    }

    private void registerCallReceiver() {
        IntentFilter callIntentFilter = new IntentFilter();
        callIntentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(callReceiver, callIntentFilter);
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(ReserverActivity.this, new String[]{
                Manifest.permission.READ_PHONE_STATE
        }, READ_PHONE_STATE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_PHONE_STATE_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                requestPermissions();
            }
        }
    }

    private void initComponents() {
        labelGuestPhone = (TextView) findViewById(R.id.label_phone);
        editTextGuestEmail = (EditText) findViewById(R.id.edittext_guest_email);
        pickerGuestsCount = (NumberPicker) findViewById(R.id.picker_guests_count);
        buttonDate = (Button) findViewById(R.id.button_date);
        buttonTime = (Button) findViewById(R.id.button_time);
        editTextAdminEmail = (EditText) findViewById(R.id.edittext_admin_email);
        buttonSubmit = (Button) findViewById(R.id.button_submit);

        pickerGuestsCount.setMinValue(GUESTS_MIN_VALUE);
        pickerGuestsCount.setMaxValue(GUESTS_MAX_VALUE);

        dateDialog = new DatePickerFragment();
        timeDialog = new TimePickerFragment();

        buttonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog.show(getFragmentManager(), "datePicker");
            }
        });

        buttonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialog.show(getFragmentManager(), "timePicker");
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (labelGuestPhone.getText().equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_missing_number),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (editTextGuestEmail.getText().toString().matches("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_missing_guest_email),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (dateDialog.getReservationDate() == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_missing_date),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (timeDialog.getReservationTime() == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_missing_time),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (editTextAdminEmail.getText().toString().matches("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_missing_admin_email),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save the email of the restaurant administrator, it will be reused.
                saveAdminEmail();

                // Add reservation to the database.
                addReservation();
                Toast.makeText(getApplicationContext(), getString(R.string.toast_saved_database), Toast.LENGTH_SHORT).show();

                // Send confirmation email to the guest and to the admin.
                sendConfirmationEmail();

                // Reset the fields to their initial state.
                resetFields();
            }
        });
    }

    public void playCallNotification() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
            ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCallReceive(String phoneNumber) {
        playCallNotification();
        labelGuestPhone.setText(phoneNumber);
        labelGuestPhone.setBackground(getDrawable(R.drawable.green_phone_box));
    }

    private void saveAdminEmail() {
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE).edit();
        editor.putString(ADMIN_EMAIL, editTextAdminEmail.getText().toString());
        editor.commit();
    }

    private void loadAdminEmail() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        editTextAdminEmail.setText(sharedPreferences.getString(ADMIN_EMAIL, ""));
    }

    private void addReservation() {
        dbProvider.addEntry(
                labelGuestPhone.getText().toString(),
                editTextGuestEmail.getText().toString(),
                pickerGuestsCount.getValue(),
                dateDialog.getReservationDate() + " " + timeDialog.getReservationTime());
    }

    private void sendConfirmationEmail() {
        String[] recipients = {editTextGuestEmail.getText().toString(), editTextAdminEmail.getText().toString()};

        String emailBody = getString(R.string.email_body_intro) +
                getString(R.string.email_body_phone) + labelGuestPhone.getText().toString() +
                getString(R.string.email_body_guest_email) + editTextGuestEmail.getText().toString() +
                getString(R.string.email_body_guests_count) + pickerGuestsCount.getValue() +
                getString(R.string.email_body_date_time) + dateDialog.getReservationDate() + " " + timeDialog.getReservationTime() +
                getString(R.string.email_body_regards);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType(getString(R.string.mime_type));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_title));
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.email_title)));
        } catch (android.content.ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(this, getString(R.string.toast_no_email_clients), Toast.LENGTH_SHORT).show();
        }
    }

    private void resetFields() {
        labelGuestPhone.setText("");
        labelGuestPhone.setBackground(getDrawable(R.drawable.red_phone_box));
        editTextGuestEmail.setText("");
        dateDialog.setReservationDate(null);
        timeDialog.setReservationTime(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbProvider.close();
    }
}