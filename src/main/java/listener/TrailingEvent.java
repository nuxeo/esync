package listener;

import db.Document;

public class TrailingEvent extends Event {

    private final Document document;

    public TrailingEvent(Document document, String message) {
        super(message);
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }
}
