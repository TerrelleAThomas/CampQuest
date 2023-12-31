package famu.edu.campusquest.Services;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.protobuf.util.Timestamps;
import famu.edu.campusquest.Model.College;
import famu.edu.campusquest.Model.Event;
import famu.edu.campusquest.Model.RestEvent;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class EventService {
    private final Firestore db = FirestoreClient.getFirestore();

    private Event getEvent(DocumentSnapshot doc) throws ExecutionException, InterruptedException {
        College collegeId = null;

        ApiFuture<DocumentSnapshot> collegeQuery = ((DocumentReference) doc.get("collegeId")).get();
        DocumentSnapshot collegeDoc = collegeQuery.get();
        collegeId = collegeDoc.toObject(College.class);

        return new Event(doc.getId(), doc.getString("eventName"), doc.getString("eventAddress"), doc.getTimestamp("eventDate"), collegeId);

    }

    public ArrayList<Event> getEvents() throws ExecutionException, InterruptedException {


        Query query = db.collection("Event");
        ApiFuture<QuerySnapshot> future = query.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        ArrayList<Event> events = (documents.size() > 0) ?  new ArrayList<>() : null;

        for(QueryDocumentSnapshot doc : documents)
        {

            events.add(getEvent(doc));
        }

        return events;
    }

    public Event getEventById(String eventId) throws ExecutionException, InterruptedException {
        Event event = null;
        DocumentReference eventDoc = db.collection("Event").document(eventId);
        ApiFuture<DocumentSnapshot> future = eventDoc.get();
        DocumentSnapshot doc = future.get();
        if(doc.exists())
            event = getEvent(doc);

        return event;
    }

    public String createEvent(RestEvent event) throws ExecutionException, InterruptedException {
        String eventId = null;

        ApiFuture<DocumentReference> future = db.collection("Event").add(event);
        DocumentReference roomRef = future.get();
        eventId = roomRef.getId();

        return eventId;
    }

    public void updateEvent(String id, Map<String, String> updateValues) throws ParseException {

        String[] allowed = {"collegeName", "eventAddress", "eventDate", "eventName"};
        List<String> list = Arrays.asList(allowed);
        Map<String, Object> formattedValues = new HashMap<>();

        for (Map.Entry<String, String> entry : updateValues.entrySet()) {
            String key = entry.getKey();
            if (list.contains(key)) {
                if (key.equals("EventDate")) {
                    formattedValues.put(key, Timestamp.fromProto(Timestamps.parse(entry.getValue())));
                } else {
                    formattedValues.put(key, entry.getValue());
                }


            }
        }

        DocumentReference eventDoc = db.collection("Event").document(id);
        eventDoc.update(formattedValues);
    }

    public void deleteEvent(String eventId){
        DocumentReference eventDoc = db.collection("Event").document(eventId);
        eventDoc.delete();
    }

}
