package org.landroo.rectangle;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Dev3 on 2016.03.11..
 */
public class WebClass
{
    private static final String TAG = "WebClass";

    private Context context;

    public WebClass(Context cont)
    {
        this.context = cont;
    }

    public void sendScores(String scores)
    {

    }

    public void updateScores()
    {

    }

    private class IpTask extends AsyncTask<String, Integer, Long>
    {
        protected Long doInBackground(String... sParams)
        {
            if(isConnected(context))
            {
                String ip = sParams[0];
                String params = "getpackages.php?t=";
                String sRes = sendRequest(ip, params, null);
            }
            return (long) 0;
        }
    }

    /**
     * send a http request
     * @param ip address
     * @param params params
     * @param file file
     * @return result
     */
    private String sendRequest(String ip, String params, File file)
    {
        String result = "error";
        HttpURLConnection con = null;
        StringBuffer answer = new StringBuffer();

        // http://a.hitter.ro:8181/php/download.php?name=aaa_200
        // http://a.hitter.ro:8181/php/getpackages.php
        String urlAddress = "http://" + ip + "/php/" + params;
        Log.i(TAG, urlAddress);
        synchronized (this)
        {
            try
            {
                // Check if task has been interrupted
                if (Thread.interrupted()) throw new InterruptedException();

                URL url = new URL(urlAddress);
                con = (HttpURLConnection) url.openConnection();
                con.setReadTimeout(5000);
                con.setConnectTimeout(5000);
                con.setRequestMethod("GET");
                con.setDoOutput(true);

                // Start the query
                con.connect();

                // Get the response
                if (file == null)
                {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                    String line;
                    while ((line = reader.readLine()) != null)
                        answer.append(line);
                    reader.close();

                    result = answer.toString();
                }
                else
                {
                    byte[] buffer = new byte[8 * 1024];
                    InputStream input = con.getInputStream();
                    try
                    {
                        OutputStream output = new FileOutputStream(file);
                        try
                        {
                            int bytesRead;
                            while ((bytesRead = input.read(buffer)) != -1)
                            {
                                output.write(buffer, 0, bytesRead);
                            }
                            result = "";
                        }
                        catch(Exception ex)
                        {
                            Log.i(TAG, "Input read error! " + ex);
                        }
                        finally
                        {
                            output.close();
                        }
                    }
                    catch(Exception ex)
                    {
                        Log.i(TAG, "OutputStream error! " + ex);
                    }
                    finally
                    {
                        input.close();
                    }
                }
            }
            catch (EOFException eo)
            {
            }
            catch (Exception ex)
            {
                // if direct communication failed try through the server
                Log.i(TAG, "sendRequest: " + urlAddress, ex);
            }
            finally
            {
                if (con != null) con.disconnect();
            }
        }
        // All done
        // Log.i(TAG, "sendRequest: " + urlAddress + "\nreturned: " + result);

        return result;
    }

    private boolean isConnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        boolean bOK = false;

        if (connectivityManager != null)
        {
            try
            {
                networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (networkInfo != null && networkInfo.isConnectedOrConnecting()) bOK = true;

                networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (networkInfo != null && networkInfo.isConnectedOrConnecting()) bOK = true;
            }
            catch (Exception ex)
            {
                Log.e(TAG, "getNetworkInfo");
            }
        }

        return bOK;
    }
}
