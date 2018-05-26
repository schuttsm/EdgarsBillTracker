package com.sschutt.billtracker3;

import android.app.Application;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.couchbase.lite.Database;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableDocument;

import static android.content.ContentValues.TAG;

/**
 * Created by Stephen on 5/19/2018.
 */

public class BillTrackerApplication extends Application {
    private Database database = null;
    private String TAG = "BillTrackerApplication";
    private JobManager jobManager;
    private String dbName = "EdgarsBillTracker";

    @Override
    public void onCreate() {
        super.onCreate();
        Configuration.Builder builder = new Configuration.Builder(this)
                .minConsumerCount(1)
                .maxConsumerCount(3)
                .loadFactor(3);
        jobManager = new JobManager(builder.build());
        openDatabase();
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    @Override
    public void onTerminate() {
        closeDatabase();
        super.onTerminate();
    }

    public void openDatabase() {
        DatabaseConfiguration config = new DatabaseConfiguration(this);
        try {
            database = new Database(this.dbName, config);
        }
        catch (CouchbaseLiteException ex) {
            Log.e(TAG, ex.toString());
            // todo add better messaging
        }
    }

    public void clearDatabase() {
        try {
            Database database = getDatabase();
            database.delete();
            openDatabase();
        }
        catch(CouchbaseLiteException e) {
            Log.e(TAG, e.toString());
        }

    }

    public Database getDatabase() {
        return database;
    }

    private void closeDatabase() {
        if (database != null) {
            try {
                database.close();
            } catch (CouchbaseLiteException e) {
                Log.e(TAG, "Failed to close Database", e);
                // TODO: error handling
            }
        }
    }
}
