package listener;

import db.Document;

public class DiffEvent extends Event {

    private final Document dbDocument;
    private final Document esDocument;

    public DiffEvent(Document dbDocument, Document esDocument, String message) {
        super(message);
        this.esDocument = esDocument;
        this.dbDocument = dbDocument;
    }

    public Document getDbDocument() {
        return dbDocument;
    }

    public Document getEsDocument() {
        return esDocument;
    }
}
