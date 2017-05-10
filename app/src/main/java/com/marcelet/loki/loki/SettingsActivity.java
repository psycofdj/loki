package com.marcelet.loki.loki;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;


public class SettingsActivity extends AppCompatActivity
{
    private ImageButton mBtnSave;
    private EditText mInputHost;
    private EditText mInputPort;
    private EditText mInputKey;
    private ProgressBar mProgressBar;
    private LokiApp mApp;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mApp         = (LokiApp) getApplication();
        mInputHost   = (EditText) findViewById(R.id.inputHost);
        mInputPort   = (EditText) findViewById(R.id.inputPort);
        mInputKey    = (EditText) findViewById(R.id.inputLogin);
        mBtnSave     = (ImageButton) findViewById(R.id.btnSave);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);


        /*
        View.OnFocusChangeListener lHandler = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                validateUi();
            }
        };
        */

        View.OnKeyListener lHandler =new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                validateUi();
            }
        };

        mInputHost.setOnKeyListener(lHandler);
        mInputPort.setOnKeyListener(lHandler);
        mInputKey.setOnKeyListener(lHandler);

        loadSettings();
        validateUi();
    }

    protected void onSaveClick()
    {
        saveSettings();
        mBtnSave.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        checkConfig();
    }

    private void onSettingsSuccess() {
        mBtnSave.getDrawable().clearColorFilter();
        mBtnSave.setColorFilter(Color.rgb(0x5c, 0x85, 0x5c));
        mProgressBar.setVisibility(View.INVISIBLE);
        finish();
    }

    private void onSettingsFailure(String pType) {
        mBtnSave.setColorFilter(Color.rgb(0xd9, 0x53, 0x4f));
        AlertDialog alertDialog = new AlertDialog.Builder(SettingsActivity.this).create();
        alertDialog.setTitle(getString(R.string.error_title));
        if (pType == "cnx")
            alertDialog.setMessage(getString(R.string.error_cnx));
        else
            alertDialog.setMessage(getString(R.string.error_key));

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();


        mProgressBar.setVisibility(View.INVISIBLE);
        mBtnSave.getDrawable().clearColorFilter();
        mBtnSave.setColorFilter(Color.rgb(0xd9, 0x53, 0x4f));
        mBtnSave.setVisibility(View.VISIBLE);
        validateUi();
        mInputHost.requestFocus();
    }

    private void loadSettings() {
        mInputHost.setText(mApp.getHost());
        mInputPort.setText(mApp.getPort());
        mInputKey.setText(mApp.getKey());
    }


    protected boolean validateUi()
    {
        mBtnSave.setOnClickListener(null);
        mBtnSave.setColorFilter(Color.rgb(175, 175, 175));
        if (mInputHost.getText().toString().isEmpty()) {
            return false;
        }
        else if (mInputPort.getText().toString().isEmpty()) {
            return false;
        }
        else if (mInputKey.getText().toString().isEmpty()) {
            return false;
        }
        mBtnSave.setColorFilter(Color.rgb(0x42, 0x8b, 0xca));
        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick();
            }
        });
        return true;
    }

    protected void saveSettings()
    {
        mApp.setHost(mInputHost.getText().toString());
        mApp.setPort(mInputPort.getText().toString());
        mApp.setKey(mInputKey.getText().toString());
        mApp.saveSettings();
    }

    protected void checkConfig()
    {
        mApp.testConfiguration(new LokiApp.TestResult() {
            @Override
            public void onSuccess() {
                onSettingsSuccess();
            }
            @Override
            public void onKeyError() {
                onSettingsFailure("key");
            }
            @Override
            public void onCnxError() {
                onSettingsFailure("cnx");
            }
        });
    }




}
