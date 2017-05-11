package com.marcelet.loki.loki;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import cz.msebera.android.httpclient.Header;

/**
 * Created by psyco on 09/05/17.
 */
public class LokiApp extends Application
{

    interface TestResult {
        void onSuccess();
        void onKeyError();
        void onCnxError();
    }

    private SharedPreferences mPrefs;
    private boolean           mValid;
    private String            mHost;
    private String            mPort;
    private String            mKey;
    static private int        msTimeoutMs = 15000;
    static private int        msRetry = 0;



    String getHost()            { return mHost; }
    String getPort()            { return mPort; }
    String getKey()             { return mKey;  }
    void setHost(String pHost)  { mHost = pHost; }
    void setPort(String pPort)  { mPort = pPort; }
    void setKey(String pKey)    { mKey  = pKey; }

    public LokiApp() {
        super();
        mValid = false;

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = getSharedPreferences("loki", 0);
        loadSettings();
    }


    public void saveSettings()
    {
        SharedPreferences.Editor lEdit = mPrefs.edit();
        lEdit.putString("host", mHost);
        lEdit.putString("port", mPort);
        lEdit.putString("key",  mKey);
        lEdit.putBoolean("valid", mValid);
        lEdit.commit();
    }

    public void loadSettings()
    {
        mHost = mPrefs.getString("host", "");
        mPort = mPrefs.getString("port", "");
        mKey  = mPrefs.getString("key", "");
        mValid = mPrefs.getBoolean("valid", false);
    }

    String getUrl(String pQuery)
    {
        Uri lUri = new Uri.Builder()
                .scheme("http")
                .authority(String.format("%s:%s", mHost, mPort))
                .path("core/api/jeeApi.php")
                .appendQueryParameter("apikey", mKey)
                .appendQueryParameter("type", "interact")
                .appendQueryParameter("query", pQuery)
                .build();

        Log.v("loki", "sending request to " + lUri.toString());
        return lUri.toString();
    }


    public void sendCommand(String pCommand, final TestResult pResult) {
        String lQuery = getUrl(pCommand);
        AsyncHttpClient lClient = new AsyncHttpClient();
        lClient.setConnectTimeout(msTimeoutMs);
        lClient.setResponseTimeout(msTimeoutMs);
        lClient.setMaxRetriesAndTimeout(msRetry, 100);
        lClient.get(lQuery, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String lString = new String(responseBody);
                Log.v("loki", "got result : " + lString);
                if (lString.contains("Clé API non valide")) {
                    mValid = false;
                    pResult.onKeyError();
                } else {
                    mValid = true;
                    pResult.onSuccess();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                mValid = false;
                //Log.v("loki", "got result : " + new String(errorResponse));
                pResult.onCnxError();
            }
        });
    }




}
