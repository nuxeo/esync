package event;

/**
 * Event to notify a difference between document type cardinality
 */
public class DiffTypeEvent extends Event {

    private final String primaryType;

    public DiffTypeEvent(String type, String message) {
        super(message);
        primaryType = type;
    }

    public String getType() {
        return primaryType;
    }
}
