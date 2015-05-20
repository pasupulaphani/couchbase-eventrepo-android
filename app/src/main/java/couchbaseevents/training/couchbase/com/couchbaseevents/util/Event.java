package couchbaseevents.training.couchbase.com.couchbaseevents.util;

/**
 * Created by phani on 18/05/2015.
 */
public class Event {
    private String name;
    private String address;
    private String eventDescription;
    private String date;
    private String time;
    private String eventType;

    private String _id;
    private String _rev;

    /**
     * Empty Constructor for JSON serialization
     */
    public Event() {

    }

    public Event(String name, String address, String eventDescription, String date,
                 String time, String eventType) {
        super();
        this.name = name;
        this.address = address;
        this.eventDescription = eventDescription;
        this.date = date;
        this.time = time;
        this.eventType = eventType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_rev() {
        return _rev;
    }

    public void set_rev(String _rev) {
        this._rev = _rev;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Name:\"").append(name);
        builder.append("\" Address:\"").append(address);
        builder.append("\" EventDescription:\"").append(eventDescription);
        builder.append("\" Date:\"").append(date);
        builder.append("\" Time:\"").append(time);
        builder.append("\" EventType:\"").append(eventType);
        builder.append("\"");

        return builder.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Event) {
            Event otherEvent = (Event) other;

            if (name.equals(otherEvent.name)
                    && address.equals(otherEvent.address)
                    && eventDescription.equals(otherEvent.eventDescription)
                    && date.equals(otherEvent.date)
                    && time.equals(otherEvent.time)
                    && eventType.equals(otherEvent.eventType)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
