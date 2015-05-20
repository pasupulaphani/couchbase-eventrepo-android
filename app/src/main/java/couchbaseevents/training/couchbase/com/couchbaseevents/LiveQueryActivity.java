package couchbaseevents.training.couchbase.com.couchbaseevents;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;

import couchbaseevents.training.couchbase.com.couchbaseevents.util.CBLSingleton;
import couchbaseevents.training.couchbase.com.couchbaseevents.util.Event;
import couchbaseevents.training.couchbase.com.couchbaseevents.util.EventRepository;
import view.PartyView;


public class LiveQueryActivity extends ActionBarActivity {

    AndroidContext context;
    EventRepository eventRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_query);

        context = new AndroidContext(getApplicationContext());

        eventRepository = new EventRepository(context);

        createLiveQuery();
        createUpdTread();
    }

    private void createLiveQuery() {
        PartyView partyView = new PartyView(context);
        partyView.createPartyView();

        Query patryQuery = partyView.getView().createQuery();
        patryQuery.setMapOnly(true);

        final LiveQuery liveQuery = patryQuery.toLiveQuery();
        liveQuery.addChangeListener(new LiveQuery.ChangeListener() {

            @Override
            public void changed(LiveQuery.ChangeEvent changeEvent) {
                Log.i(CBLSingleton.TAG, "Change event received");
            }
        });
    }

    private void createUpdTread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Event event = new Event("New Party", "123 Elm St",
                        "Anyone is invited!", "2014-12-24", "18:00:00",
                        "party");
                eventRepository.save(event);

                Log.i(CBLSingleton.TAG, "Adding event");

                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    Log.e(CBLSingleton.TAG, "Sleep interrupted");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_live_query, menu);
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
