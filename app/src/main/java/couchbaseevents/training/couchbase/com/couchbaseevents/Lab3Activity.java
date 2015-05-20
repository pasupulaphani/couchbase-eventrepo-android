package couchbaseevents.training.couchbase.com.couchbaseevents;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;

import couchbaseevents.training.couchbase.com.couchbaseevents.util.CBLSingleton;
import couchbaseevents.training.couchbase.com.couchbaseevents.util.Event;
import couchbaseevents.training.couchbase.com.couchbaseevents.util.EventRepository;


public class Lab3Activity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab3);

        // refactored hello world
        EventRepository eventRepository = new EventRepository(
                new AndroidContext(getApplicationContext())
        );

        Event event = new Event("Refactored party", "buckinkham palace", "queen is paying", "2015-05-18", "12:00:00", "rave");
        eventRepository.save(event);

        event = eventRepository.get(event.get_id());
        Log.i(CBLSingleton.TAG, "Event Object Created:" + event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lab3, menu);
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
