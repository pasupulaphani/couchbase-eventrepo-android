package couchbaseevents.training.couchbase.com.couchbaseevents;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;

import org.codehaus.jackson.map.ObjectMapper;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import couchbaseevents.training.couchbase.com.couchbaseevents.util.CBLSingleton;
import couchbaseevents.training.couchbase.com.couchbaseevents.util.Event;
import couchbaseevents.training.couchbase.com.couchbaseevents.util.EventRepository;


public class ConflictResolveActivity extends ActionBarActivity {
    AndroidContext context;
    EventRepository eventRepository;
    Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conflict_resolve);

        context = new AndroidContext(getApplicationContext());
        eventRepository = new EventRepository(context);
        database = CBLSingleton.getInstance(context).getDatabase();

        Event event1 = new Event("Conflict Party", "123 Elm St",
                "Anyone is invited!", "2014-12-24", "18:00:00", "party");

        Event event2 = new Event("Another Conflicted Party", "123 Elm St",
                "Anyone is invited!", "2014-12-24", "18:00:00", "party");

        // Create and process the first round of conflicts
        createConflict1(event1, database);
        createConflict2(event2, database);

        outputConflicts();
        resolveConflicts();
        outputConflicts();
    }

    private void outputConflicts() {
        Log.i(CBLSingleton.TAG, "Outputting conflicts");

        try {
            // Create a query to find all conflicts
            Query allDocsQuery = database.createAllDocumentsQuery();
            allDocsQuery.setAllDocsMode(Query.AllDocsMode.ONLY_CONFLICTS);
            QueryEnumerator rows = allDocsQuery.run();

            // Output all conflicts found
            for (Iterator<QueryRow> it = rows; it.hasNext();) {
                QueryRow row = it.next();

                Log.i(CBLSingleton.TAG, "Found conflict:" + row.getKey());
            }
        } catch (Exception e) {
            Log.e(CBLSingleton.TAG, "Error outputting conflicts", e);
        }

    }

    /**
     * Creates the first round of a conflict
     *
     * @param event1
     *            The event to create a conflict with
     *
     * @param database
     *            The database to use
     */
    private void createConflict1(Event event1, Database database) {
        Document doc = database.createDocument();

        try {
            SavedRevision rev1 = doc.createRevision().save();

            // Simulate the same event being added twice in two different
            // locations
            event1.set_id(UUID.randomUUID().toString());
            createRevisionWithRandomProps(rev1, event1, false);

            event1.set_id(UUID.randomUUID().toString());
            createRevisionWithRandomProps(rev1, event1, true);
        } catch (Exception e) {
            Log.e(CBLSingleton.TAG, "Error creating conflict", e);
        }
    }

    /**
     * Creates the second round of a conflict
     *
     * @param event
     *            The event to create a conflict with
     * @param database
     *            The database to use
     */
    private void createConflict2(Event event, Database database) {
        Document doc = database.createDocument();

        try {
            // Simulate the same event being updated twice
            event.set_id(UUID.randomUUID().toString());
            SavedRevision rev2 = doc.createRevision().save();

            createRevisionWithRandomProps(rev2, event, false);

            event.setName(event.getName() + "-DRAFT");
            createRevisionWithRandomProps(rev2, event, true);
        } catch (Exception e) {
            Log.e(CBLSingleton.TAG, "Error creating conflict", e);
        }
    }
    private void resolveConflicts() {
        Log.i(CBLSingleton.TAG, "Resolving conflicts");

        try {
            // Create a query to find all conflicts
            Query allDocsQuery = database.createAllDocumentsQuery();
            allDocsQuery.setAllDocsMode(Query.AllDocsMode.ONLY_CONFLICTS);
            QueryEnumerator rows = allDocsQuery.run();

            // Process all conflicts
            for (Iterator<QueryRow> it = rows; it.hasNext();) {
                QueryRow row = it.next();

                Document conflictedDocument = database.getDocument((String) row
                        .getKey());
                List<SavedRevision> conflictingRevisions = conflictedDocument
                        .getConflictingRevisions();

                if (conflictingRevisions.size() == 2) {
                    SavedRevision rev1 = conflictingRevisions.get(0);
                    SavedRevision rev2 = conflictingRevisions.get(1);

                    if (rev1.getProperties().equals(rev2.getProperties())) {
                        // The two events contain the same data, delete the
                        // second revision. This does not delete whole document
                        // just the revision.
                        rev2.deleteDocument();
                        Log.i(CBLSingleton.TAG, "Deleting duplicated event.");
                    } else {
                        // The two events contain different data. Figure out
                        // which one to delete by looking to see if one is a
                        // DRAFT
                        processDrafts(rev1, rev2);
                    }
                } else {
                    Log.i(CBLSingleton.TAG, "Too many conflicts to process:"
                            + conflictingRevisions.size());
                }
            }
        } catch (Exception e) {
            Log.e(CBLSingleton.TAG, "Error outputting conflicts", e);
        }

        Log.i(CBLSingleton.TAG, "Finished resolving conflicts");
    }
    private void processDrafts(SavedRevision rev1, SavedRevision rev2)
            throws CouchbaseLiteException {
        String rev1Name = (String) rev1.getProperty(EventRepository.NAME_FIELD);
        String rev2Name = (String) rev2.getProperty(EventRepository.NAME_FIELD);

        boolean hasDraft1 = rev1Name.contains("DRAFT");
        boolean hasDraft2 = rev2Name.contains("DRAFT");

        // Is one revision a draft?
        if (hasDraft1 == false && hasDraft2 == false) {
            Log.i(CBLSingleton.TAG,
                    "Neither revision is a draft. Leaving alone");
        } else {
            // Remove the revision that was a draft
            if (hasDraft1 == true) {
                rev1.deleteDocument();
                Log.i(CBLSingleton.TAG, "Rev1 is a draft. Deleting");
            } else {
                rev2.deleteDocument();
                Log.i(CBLSingleton.TAG, "Rev2 is a draft. Deleting");
            }
        }
    }

    /**
     * Creates a revision and manually allows for conflicts
     *
     * @param createRevFrom
     *            The revision to create new revision from
     * @param event
     *            The event to base the revision on
     * @param allowConflict
     *            Whether or not to allow a conflict
     * @return A new revision that can be in conflict
     * @throws Exception
     */
    private SavedRevision createRevisionWithRandomProps(
            SavedRevision createRevFrom, Event event, boolean allowConflict)
            throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> objectMap = mapper.convertValue(event, Map.class);

        UnsavedRevision unsavedRevision = createRevFrom.createRevision();
        unsavedRevision.setUserProperties(objectMap);
        return unsavedRevision.save(allowConflict);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conflict_resolve, menu);
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
