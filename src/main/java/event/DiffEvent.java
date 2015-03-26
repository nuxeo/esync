package event;

import db.Document;

/**
 * Event to notify difference between a document in db and es
 */
public class DiffEvent extends Event {

    private final Document dbDocument;
    private final Document esDocument;

    public DiffEvent(Document dbDocument, Document esDocument, String message) {
        super(message);
        this.dbDocument = dbDocument;
        this.esDocument = esDocument;
    }

    public Document getDbDocument() {
        return dbDocument;
    }

    public Document getEsDocument() {
        return esDocument;
    }
}
