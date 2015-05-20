package couchbaseevents.training.couchbase.com.couchbaseevents;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class HelloWorldActivity extends ActionBarActivity {
    public static final String DB_NAME = "couchbaseevents";
    public static final String TAG = "couchbaseevents";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);

        // set up couchbase database connection
        Manager manager = null;
        Database database = null;

        try {
            manager = new Manager(
                    new AndroidContext(getApplicationContext()),
                    manager.DEFAULT_OPTIONS);
            database = manager.getDatabase(DB_NAME);

        } catch (Exception e) {
            Log.e(TAG, "Error getting dtabase");
            e.printStackTrace();
            return;
        }
        // Create the document
        String documentId = createDocument(database);

        // Get and output the contents
        outputContents(database, documentId);

        // Update the document and add an attachment
        updateDoc(database, documentId);

        // Add an attachment
        addAttachment(database, documentId);

        // Get and output the contents with the attachment
        outputContentsWithAttachment(database, documentId);

    }

    private String createDocument(Database database) {
        Document document=database.createDocument();
        String documentId= document.getId();
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("name","BigParty");
        map.put("address", "123 Beverly hills");

        try {
            document.putProperties(map);
        } catch (Exception e) {
            Log.e(DB_NAME, "Error creating", e);
        }

        return documentId;
    }

    private void outputContents(Database database, String documentId) {
        // Get the document and output all of the contents
        Document getDocument = database.getExistingDocument(documentId);

        if (getDocument != null) {
            for (Map.Entry<String, Object> property : getDocument
                    .getProperties().entrySet()) {
                Log.i(TAG, "Property Key:" + property.getKey() + " Value:"
                        + property.getValue());
            }
        } else {
            Log.i(TAG, "The Document was null");
        }
    }

    private void updateDoc(Database database, String documentId) {
        Document getDocument = database.getDocument(documentId);

        try {
            // Update the document with more data
            Map<String, Object> updatedProperties = new HashMap<String, Object>();
            updatedProperties.putAll(getDocument.getProperties());

            updatedProperties.put("eventDescription", "Anyone is invited!");
            updatedProperties.put("address", "123 Elm St.");

            // Save the properties to the document
            getDocument.putProperties(updatedProperties);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Error putting", e);
        }
    }

    private void addAttachment(Database database, String documentId) {
        Document getDocument = database.getDocument(documentId);

        try {
            // Add an attachment
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    new byte[] { 0, 0, 0, 0 });

            UnsavedRevision revision = getDocument.getCurrentRevision()
                    .createRevision();
            revision.setAttachment("zeros.bin", "application/octet-stream",
                    inputStream);

            // Save the attachment to document
            revision.save();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Error putting", e);
        }
    }

    private void deleteDocument(Database database, String documentId) {
        // Get the updated document and output all of the contents
        Document deleteDocument = database.getDocument(documentId);

        try {
            boolean deleted = deleteDocument.delete();

            Log.e(TAG, "Status after delete of " + documentId + " was "
                    + deleted);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Error deleting", e);
        }
    }

    /**
     * Outputs if the Document is deleted
     *
     * @param database
     *            The CBL database
     * @param documentId
     *            The Id of the Document to output
     */
    private void outputIsDeleted(Database database, String documentId) {
        // Get the updated document and output all of the contents
        Document deleteDocument = database.getDocument(documentId);

        Log.i(TAG, "Delete status for:" + documentId + " deleted="
                + deleteDocument.isDeleted());
    }

    private void outputContentsWithAttachment(Database database,
                                              String documentId) {
        // Get the updated document and output all of the contents
        Document upatedDocument = database.getDocument(documentId);

        if (upatedDocument != null) {
            Log.i(TAG, "The Document was null");
        }

        for (Map.Entry<String, Object> property : upatedDocument
                .getProperties().entrySet()) {
            Log.i(TAG, "Updated Property Key:" + property.getKey() + " Value:"
                    + property.getValue());
        }

        try {
            // Output the contents of the attachment
            SavedRevision savedRevision = upatedDocument.getCurrentRevision();
            Attachment attachment = savedRevision.getAttachment("zeros.bin");

            int readValue = 0;

            StringBuilder builder = new StringBuilder();

            while ((readValue = attachment.getContent().read()) != -1) {
                builder.append(readValue).append(",");
            }

            Log.i(TAG, "The value of the attachement is:" + builder.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error reading attachment", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hello_world, menu);
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
