package event;

public class MissingEvent extends Event {

    private final String docId;

    public MissingEvent(String docId, String message) {
        super(message);
        this.docId = docId;
    }

    public String getDocId() {
        return docId;
    }
}
