package event;

public class TrailingEvent extends Event {

    private final String docId;

    public TrailingEvent(String docId, String message) {
        super(message);
        this.docId = docId;
    }

    public String getDocId() {
        return docId;
    }
}
