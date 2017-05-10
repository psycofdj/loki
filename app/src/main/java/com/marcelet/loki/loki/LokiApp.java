package com.marcelet.loki.loki;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.concurrent.Callable;

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
    static private int        msTimeoutMs = 30000;
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

    public void testConfiguration(final TestResult pRessult)
    {
        String lQuery = getUrl("null");
        AsyncHttpClient lClient = new AsyncHttpClient();
        lClient.setMaxRetriesAndTimeout(msRetry, msTimeoutMs);
        lClient.get(lQuery, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String lString = new String(responseBody);
                if (lString.contains("Clé API non valide")) {
                    mValid = false;
                    pRessult.onKeyError();
                } else {
                    mValid = true;
                    pRessult.onSuccess();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                mValid = false;
                pRessult.onCnxError();
            }
        });
    }




}
