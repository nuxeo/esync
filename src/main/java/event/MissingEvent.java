package event;

import db.Document;

public class MissingEvent extends Event {

    private final Document document;

    public MissingEvent(Document document, String message) {
        super(message);
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }
}
