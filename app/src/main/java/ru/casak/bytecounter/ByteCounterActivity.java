package ru.casak.bytecounter;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;


public class ByteCounterActivity extends AppCompatActivity {

    private final static String TAG = ByteCounterActivity.class.getSimpleName();
    private final static String HOSTNAME = "speedtest.tele2.net";
    private final static String FILENAME = "1MB.zip";
    private final static Integer TIMER_MINUTES = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new MyTask().execute();
    }

    class MyTask extends AsyncTask<Void, Void, Integer> {
        private final String TAG = MyTask.class.getSimpleName();
        BigInteger counter = new BigInteger("0");

        @Override
        protected void onPreExecute() {
            new CountDownTimer(TIMER_MINUTES * 60 * 1000, 1000) {

                public void onTick(long millisUntilFinished) {
                    Log.d(TAG, "seconds remaining: " + millisUntilFinished / 1000);
                }

                public void onFinish() {
                    onPostExecute(0);
                    System.exit(0);
                }
            }.start();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            FTPClient client = new FTPClient();
            try {
                client.connect(HOSTNAME);
                client.enterLocalPassiveMode();
                client.setFileType(FTP.BINARY_FILE_TYPE);
                client.login("anonymous", "anonymous");
                String remoteFile = "/" + FILENAME;
                while (true) {
                    InputStream inputStream = client.retrieveFileStream(remoteFile);
                    byte[] bytesArray = new byte[4096];
                    int bytesRead = -1;
                    if (inputStream == null) {
                        Log.d(TAG, "could not retrieve file: " + FILENAME);
                        continue;
                    }
                    while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                        counter = counter.add(new BigInteger(bytesRead + ""));
                    }
                    Log.d(TAG, "file: " + FILENAME +" is downloaded");
                    client.completePendingCommand();
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try{
                    client.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer param) {
            Log.d(TAG, "Total bytes: " + counter.toString());
        }
    }
}
