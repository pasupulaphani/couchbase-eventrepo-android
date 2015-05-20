package couchbaseevents.training.couchbase.com.couchbaseevents.util;

/**
 * Created by phani on 18/05/2015.
 */
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;
import couchbaseevents.training.couchbase.com.couchbaseevents.util.Event;

public class EventRepository {
    /** The Android Context */
    AndroidContext context;

    /** The object mapper for translating objects */
    ObjectMapper mapper = new ObjectMapper();

    public static final String NAME_FIELD = "name";
    public static final String ADDRESS_FIELD = "address";
    public static final String EVENT_DESCRIPTION_FIELD = "eventDescription";
    public static final String DATE_FIELD = "date";
    public static final String TIME_FIELD = "time";
    public static final String EVENT_TYPE_FIELD = "eventType";
    public static final String ID_FIELD = "_id";
    public static final String REV_FIELD = "_rev";

    public EventRepository(AndroidContext context) {
        this.context = context;
    }

    /**
     * Saves a new event
     *
     * @param event
     *            The event to save
     * @return The saved event with its ID
     */
    public <S extends Event> S save(S event) {
        Document document = CBLSingleton.getInstance(context).getDatabase()
                .createDocument();

        return saveEvent(document, event);
    }

    /**
     * Updates an existing event
     *
     * @param event
     *            The event to update
     * @return The updated event with its information or null if the document
     *         does not exist already
     */
    public <S extends Event> S update(S event) {
        Document document = getDocumentById(event.get_id());

        if (document == null) {
            return null;
        }

        return saveEvent(document, event);
    }

    /**
     * Gets an event based on ID
     *
     * @param id
     *            The ID of the event
     * @return The event with the ID or null if not found
     */
    public Event get(String id) {
        Document getDocument = getDocumentById(id);

        if (getDocument == null) {
            return null;
        }

        try {
            Event event = mapper.convertValue(getDocument.getProperties(),
                    Event.class);

            return event;
        } catch (Exception e) {
            Log.e(CBLSingleton.TAG, "Error creating class", e);
            return  null;
        }
    }

    /**
     * Checks if the ID exists
     *
     * @param id
     *            The event id
     * @return True if the event ID exists
     */
    public boolean exists(String id) {
        return getDocumentById(id) != null;
    }

    /**
     * Deletes an event
     *
     * @param event
     *            The Event to delete
     * @return True if successful
     */
    public <S extends Event> boolean delete(S event) {
        try {
            return getDocumentById(event.get_id()).delete();
        } catch (CouchbaseLiteException e) {
            Log.e(CBLSingleton.TAG, "Error deleting", e);

            return false;
        }
    }

    private Document getDocumentById(String id) {
        return CBLSingleton.getInstance(context).getDatabase().getDocument(id);
    }

    @SuppressWarnings("unchecked")
    private <S extends Event> S saveEvent(Document document, S event) {
        try {
            Map<String, Object> objectMap = mapper.convertValue(event,
                    Map.class);
            document.putProperties(objectMap);

            event.set_id(document.getId());

            // why? this changes everytime
            event.set_rev(document.getCurrentRevisionId());

            return event;
        } catch (CouchbaseLiteException e) {
            Log.e(CBLSingleton.TAG, "Error putting", e);

            return null;
        }
    }
}
