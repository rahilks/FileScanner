package com.rahil.filescanner;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rahil.filescanner.adapter.ExtensionsAdapter;
import com.rahil.filescanner.adapter.LargeFileAdapter;
import com.rahil.filescanner.data.FileData;
import com.rahil.filescanner.fragment.ProgressDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class FileScanActivity extends AppCompatActivity implements View.OnClickListener, ProgressDialogFragment.ProgressDialogActionListener {

    private static final String TAG_PROGRESS_DIALOG = "TAG_PROGRESS_DIALOG";

    private long avgFileSize;
    int fileCount = 0;
    int totalFileSize = 0;
    TextView avgSizeTextView;
    HashMap<String, Integer> extensionHashMap = new HashMap();
    PriorityQueue<FileData> fileSizeQueue = new PriorityQueue<>(10, Collections.reverseOrder());
    private RecyclerView mLargestFileRecyclerView;
    private Adapter mLargestFileAdapter;
    private RecyclerView mExtensionRecyclerView;
    private Adapter mExtensionsAdapter;
    private Button mShareButton;
    private Button mScanButton;
    private String mSharingText;
    private String[] topFiveExtText;
    private FileData[] fileDataArray;
    private View scanResultLayout;
    private int mNotificationId = 1;
    private NotificationManager mNotificationManager;
    private ScanSDCardTask scanSDCardTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_scan);
        scanResultLayout = findViewById(R.id.scan_result_layout);
        mLargestFileRecyclerView = (RecyclerView) findViewById(R.id.largest_recycler_view);
        mLargestFileRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mExtensionRecyclerView = (RecyclerView) findViewById(R.id.extensions_recycler_view);
        mExtensionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        avgSizeTextView = (TextView) findViewById(R.id.avg_file_size);
        mShareButton = (Button) findViewById(R.id.share_button);
        mScanButton = (Button) findViewById(R.id.start_scan_button);
        mShareButton.setOnClickListener(this);
        mScanButton.setOnClickListener(this);
        mSharingText = getString(R.string.scan_result_title);

        scanSDCardTask = new ScanSDCardTask();
        showProgressDialog();
        showNotification();
        scanSDCardTask.execute();
    }


    private void showNotification() {
        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.stat_notify_sdcard)
                        .setContentTitle(getString(R.string.sd_card_notif_title))
                        .setContentText(getString(R.string.scanning_sd_card))
                        .setOngoing(true);
        Intent resultIntent = new Intent(this, FileScanActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(FileScanActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }

    @Override
    public void onStopClicked() {
        scanSDCardTask.cancel(true);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(mNotificationId);
        }
        mScanButton.setVisibility(View.VISIBLE);
    }

    private class ScanSDCardTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            File dir = Environment.getExternalStorageDirectory();
            getListOfFiles(dir);
            if (fileCount > 0) {
                avgFileSize = totalFileSize / fileCount;
                String avgFileSizeText = getString(R.string.avg_file_size_text) + avgFileSize;
                mSharingText += "\n" + avgFileSizeText;

                LinkedHashMap<String, Integer> sortedMap = sortHashMapByValues(extensionHashMap);

                topFiveExtText = new String[5];
                Iterator it = sortedMap.entrySet().iterator();
                int count = 0;
                mSharingText += "\n" + getString(R.string.most_frequent_extensions);
                while (it.hasNext() && count < 5) {
                    Map.Entry pair = (Map.Entry) it.next();
                    mSharingText += "\n\t Extension-" + pair.getKey() + " Frequency- " + pair.getValue();
                    topFiveExtText[count] = "Extension-" + pair.getKey() + " Frequency- " + pair.getValue();
                    it.remove();
                    count++;
                }
                mSharingText += "\n" + getString(R.string.largest_files);
                fileDataArray = new FileData[10];
                for (int i = 0; i < 10; i++) {
                    fileDataArray[i] = fileSizeQueue.remove();
                    mSharingText += "\n\t" + "File Name- " + fileDataArray[i].getFileName() + " File Size- " + fileDataArray[i].getSize() + " Bytes";
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {

            if (fileCount > 0) {
                System.out.println("avg " + avgFileSize);

                avgSizeTextView.setText(getString(R.string.avg_file_size_text) + avgFileSize);
                mLargestFileAdapter = new LargeFileAdapter(fileDataArray);
                mLargestFileRecyclerView.setAdapter(mLargestFileAdapter);

                mExtensionsAdapter = new ExtensionsAdapter(topFiveExtText);
                mExtensionRecyclerView.setAdapter(mExtensionsAdapter);
                scanResultLayout.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(FileScanActivity.this, R.string.scan_error, Toast.LENGTH_LONG).show();
            }
            dismissProgressDialog();
            if (mNotificationManager != null) {
                mNotificationManager.cancel(mNotificationId);
            }
        }
    }

    public LinkedHashMap<String, Integer> sortHashMapByValues(
            HashMap<String, Integer> passedMap) {
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Integer> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues, Collections.reverseOrder());
        Collections.sort(mapKeys);

        LinkedHashMap<String, Integer> sortedMap =
                new LinkedHashMap<>();

        Iterator<Integer> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Integer val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Integer num1 = passedMap.get(key);
                Integer num2 = val;

                if (num1.equals(num2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    void getListOfFiles(File directory) {
        File[] paths = directory.listFiles();
        if (paths != null) {
            for (File file : paths) {
                if (file.isDirectory()) {
                    getListOfFiles(file);
                } else {
                    System.out.println(file.getName() + "-" + file.length());

                    String filenameArray[] = file.getName().split("\\.");
                    String extension = filenameArray[filenameArray.length - 1];

                    fileSizeQueue.add(new FileData(file.length(), file.getName()));

                    Integer count = extensionHashMap.get(extension);
                    if (count != null) {
                        extensionHashMap.put(extension, count.intValue() + 1);
                    } else {
                        extensionHashMap.put(extension, 1);
                    }
                    fileCount++;
                    totalFileSize += file.length();
                }
            }
        }
    }

    private void showProgressDialog() {
        ProgressDialogFragment.newInstance(getString(R.string.scanning_sd_card))
                .show(getFragmentManager(), TAG_PROGRESS_DIALOG);
    }

    private void dismissProgressDialog() {
        ProgressDialogFragment progressDialog = (ProgressDialogFragment) getFragmentManager().findFragmentByTag(TAG_PROGRESS_DIALOG);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_button:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mSharingText);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.send_file_scan_title)));
                break;
            case R.id.start_scan_button:
                showProgressDialog();
                showNotification();
                mScanButton.setVisibility(View.GONE);
                scanSDCardTask = new ScanSDCardTask();
                scanSDCardTask.execute();
                break;

        }
    }
}
