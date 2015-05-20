package couchbaseevents.training.couchbase.com.couchbaseevents;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;

import java.util.Iterator;

import couchbaseevents.training.couchbase.com.couchbaseevents.util.CBLSingleton;
import couchbaseevents.training.couchbase.com.couchbaseevents.util.Event;
import couchbaseevents.training.couchbase.com.couchbaseevents.util.EventRepository;
import view.PartyView;

public class ViewActivity extends ActionBarActivity {
    AndroidContext context;

    EventRepository eventRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        context = new AndroidContext(getApplicationContext());

        eventRepository = new EventRepository(context);

        new PartyView(context).createPartyView();
        addMoreEvents();

        outputAllParties();
        outputNumberOfParties();

        // Optional Advanced Full Text
//        outputFullText("folk");
    }

    private void addMoreEvents() {
        // Create some parties
        Event event = new Event("Big Party", "123 Elm St",
                "Anyone is invited!", "2014-12-24", "18:00:00", "party");
        eventRepository.save(event);

        Event event2 = new Event("Another Big Party", "123 Elm St",
                "Anyone is invited!", "2014-12-25", "16:00:00", "party");
        eventRepository.save(event2);

        Event event3 = new Event("Birthday Party", "123 Elm St",
                "Anyone is invited!", "2014-12-30", "17:00:00", "party");
        eventRepository.save(event3);

        // Create some non-parties
        Event event4 = new Event("Oates and Garfunkel", "1600 Main St",
                "The best in folk", "2014-12-30", "17:00:00", "concert");
        eventRepository.save(event4);

        Event event5 = new Event("Oates and Garfunkel", "1600 Main St",
                "The best in folk", "2014-12-31", "17:00:00", "concert");
        eventRepository.save(event5);
    }

    private void outputAllParties() {
        // Instantiate the view
        Query partyQuery = new PartyView(context).getView().createQuery();

        // Run the query as Map-only
        partyQuery.setMapOnly(true);

        try {
            QueryEnumerator result = partyQuery.run();

            // Iterate through all parties and output their events
            for (Iterator<QueryRow> it = result; it.hasNext();) {
                QueryRow row = it.next();

                Event event = eventRepository.get((String) row.getDocumentId());
                Log.i(CBLSingleton.TAG, "Found party:" + event);
            }
        } catch (CouchbaseLiteException e) {
            Log.e(CBLSingleton.TAG, "Error querying view.", e);
        }

    }

    /**
     * Outputs the number of parties that were found
     */
    private void outputNumberOfParties() {
        // Instantiate the view
        Query partyQuery = new PartyView(context).getView().createQuery();

        // Run the query with the reducer too
        partyQuery.setMapOnly(false);

        try {
            QueryEnumerator result = partyQuery.run();

            // Iterate through the single row and output the amount
            for (Iterator<QueryRow> it = result; it.hasNext();) {
                QueryRow row = it.next();

                Integer numberOfParties = (Integer) row.getValue();

                Log.i(CBLSingleton.TAG, "Number of parties:" + numberOfParties);
            }
        } catch (CouchbaseLiteException e) {
            Log.e(CBLSingleton.TAG, "Error querying view.", e);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view, menu);
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
