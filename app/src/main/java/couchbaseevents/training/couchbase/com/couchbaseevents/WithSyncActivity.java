package couchbaseevents.training.couchbase.com.couchbaseevents;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.couchbase.lite.Database;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import couchbaseevents.training.couchbase.com.couchbaseevents.util.CBLSingleton;
import couchbaseevents.training.couchbase.com.couchbaseevents.util.Event;
import couchbaseevents.training.couchbase.com.couchbaseevents.util.EventRepository;


public class WithSyncActivity extends ActionBarActivity {

    private AndroidContext context;

    /**
     * The URL for the Sync Gateway.<br>
     * Note: 10.0.2.2 == Android Simulator equivalent of 127.0.0.1
     */
    public static final String GATEWAY_SYNC_URL = "http://10.0.2.2:4984/"
            + CBLSingleton.DB_NAME;

    private Replication.ReplicationStatus pullStatus, pushStatus;
    int pullCompletedChanges;
    int pullChanges;

    int pushCompletedChanges;
    int pushChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_sync);

        context = new AndroidContext(getApplicationContext());

        replicateCBL();
    }

    private void replicateCBL() {
        createPushReplication();
        createPullReplication();

        updateThread();
        statusThread();
    }

    private void createPushReplication() {
        Database database = CBLSingleton.getInstance(context).getDatabase();

        try {
            Replication push = database.createPushReplication(new URL(GATEWAY_SYNC_URL));

            push.addChangeListener(new Replication.ChangeListener() {

                @Override
                public void changed(Replication.ChangeEvent event) {
                    Replication push = event.getSource();

                    pushStatus = push.getStatus();

                    pushCompletedChanges = push.getCompletedChangesCount();
                    pushChanges = push.getChangesCount();
                    Log.i(CBLSingleton.TAG, "PUSH completedChangesCount :" + pushCompletedChanges );
                    Log.i(CBLSingleton.TAG, "PUSH changesCount :" + pushChanges );
                }
            });

            push.setContinuous(true);
            push.start();
        } catch (MalformedURLException e) {
            Log.e(CBLSingleton.TAG, "Error creating pull", e);
        }
    }

    private void createPullReplication() {
        Database database = CBLSingleton.getInstance(context).getDatabase();

        try {
            Replication pull = database.createPullReplication(new URL(GATEWAY_SYNC_URL));

            pull.addChangeListener(new Replication.ChangeListener() {

                @Override
                public void changed(Replication.ChangeEvent event) {
                    Replication pull = event.getSource();

                    pullStatus = pull.getStatus();

                    pullCompletedChanges = pull.getCompletedChangesCount();
                    pullChanges = pull.getChangesCount();
                    Log.i(CBLSingleton.TAG, "PULL completedChangesCount :" + pullCompletedChanges );
                    Log.i(CBLSingleton.TAG, "PULL changesCount :" + pullChanges );
                }
            });

            pull.setContinuous(true);
            pull.start();
        } catch (MalformedURLException e) {
            Log.e(CBLSingleton.TAG, "Error creating pull", e);
        }
    }

    private void updateThread() {
        final EventRepository eventRepository = new EventRepository(
                new AndroidContext(getApplicationContext()));

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                // Create the document
                Event event = new Event("Updating Event", "123 Elm St",
                        "Continuously Updated", "2014-12-24", "18:00:00",
                        "misc");

                eventRepository.save(event);

                for (int i = 0; i < 20; i++) {
                    // Update the document in the thread
                    event.setName(event.getName() + i);
                    eventRepository.update(event);

                    Log.i(CBLSingleton.TAG, "Event updated:" + event);

                    try {
                        // Sleep for 2 seconds
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.e(CBLSingleton.TAG, "Sleep interrupted", e);
                    }
                }
            }
        });

        thread.start();

    }

    private void statusThread() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    Log.i(CBLSingleton.TAG,
                            "Pull:"
                                    + getSyncStatus(pullStatus,
                                    pullCompletedChanges, pullChanges));
                    Log.i(CBLSingleton.TAG,
                            "Push:"
                                    + getSyncStatus(pushStatus,
                                    pushCompletedChanges, pushChanges));

                    try {
                        // Sleep for 2 seconds
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.e(CBLSingleton.TAG, "Sleep interrupted", e);
                    }
                }
            }
        });

        thread.start();
    }

    private String getSyncStatus(Replication.ReplicationStatus status, int completedChanges, int changes) {
        String statusString;

        if (status == null) {
            return null;
        }
        switch (status) {
            case REPLICATION_ACTIVE:
                if (changes != completedChanges) {
                    statusString = "Need to sync " + (changes - completedChanges)
                            + " changes";
                } else {
                    statusString = "All caught up!";
                }
                break;
            case REPLICATION_IDLE:
                statusString = "All caught up Idle";
                break;
            case REPLICATION_OFFLINE:
                statusString = "Currently offline. Will sync when there is a connection.";
                break;
            case REPLICATION_STOPPED:
                statusString = "Replication stopped. Restart the app!";
                break;
            default:
                statusString = "UNKNOWN";
                break;
        }

        return statusString;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_with_sync, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
