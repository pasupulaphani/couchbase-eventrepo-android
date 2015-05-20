package couchbaseevents.training.couchbase.com.couchbaseevents.util;

import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;

/**
 * Created by phani on 18/05/2015.
 */
public class CBLSingleton {

    private static CBLSingleton instance;

    public static final String DB_NAME = "couchbaseevents";
    public static final String AUTH_USERNAME = "appuser";
    public static final String AUTH_PASSWORD = "apppassword";
    public static final String TAG = "couchbaseevents";

    Manager manager = null;
    Database database = null;

    /**
     * Private constructor to only allow getInstance to initialize
     */
    private CBLSingleton() {

    }

    public synchronized static CBLSingleton getInstance(AndroidContext context) {
        if (instance == null) {
            instance = new CBLSingleton();

            try {
                instance.manager = new Manager(context, Manager.DEFAULT_OPTIONS);
                instance.database = instance.manager.getDatabase(DB_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Error getting database", e);

                instance = null;
            }
        }

        return instance;
    }

    public Manager getManager() {
        return manager;
    }

    public Database getDatabase() {
        return database;
    }
}
