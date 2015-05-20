package view;

import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Reducer;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;

import java.util.List;
import java.util.Map;

import couchbaseevents.training.couchbase.com.couchbaseevents.util.CBLSingleton;
import couchbaseevents.training.couchbase.com.couchbaseevents.util.EventRepository;

/**
 * Created by phani on 20/05/2015.
 */
public class PartyView {
    /** The name of the view with all of the parties */
    public static final String PARTIES_VIEW = "parties";

    AndroidContext context;

    public PartyView(AndroidContext context) {
        this.context = context;
    }

    public void createPartyView() {
        Database database = CBLSingleton.getInstance(context).getDatabase();

        View partyView = database.getView(PARTIES_VIEW);

        partyView.setMapReduce(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                String eventType = (String) document.get(EventRepository.EVENT_TYPE_FIELD);

                if (eventType != null && eventType.equals("party")) {
                    emitter.emit(document.get(EventRepository.EVENT_TYPE_FIELD), null);
                }
            }
        }, new Reducer() {
            @Override
            public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
                return Integer.valueOf(values.size());
            }
        }, "1");
    }

    public View getView() {
        Database database = CBLSingleton.getInstance(context).getDatabase();
        return database.getView(PARTIES_VIEW);
    }
}
