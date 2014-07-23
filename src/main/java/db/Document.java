package db;

public class Document {
    public String id;
    public String acl;
    public String path;
    public String parentId;

    public Document(String id, String acl, String path) {
        this.id = id;
        this.acl = acl;
        this.path = path;
    }

    public Document(String id, String acl) {
        this.id = id;
        this.acl = acl;
    }

    @Override
    public String toString() {
        return String.format("<doc id=%s acl=%s path=%s parentid=%s />", id, acl, path, parentId);
    }


    @Override
    public boolean equals(Object obj) {
        Document other = (Document) obj;
        if (id != null && ! id.equals(other.id)) {
            return false;
        }
        if (acl != null && ! acl.equals(other.acl)) {
            return false;
        }
        if (! acl.equals(other.acl)) {
            return false;
        }
        if (path != null && ! path.equals(other.path)) {
            return false;
        }
        if (parentId != null && ! parentId.equals(other.parentId)) {
            return false;
        }
        return true;
    }

    public void merge(Document other) {
        this.path = other.path;
        this.parentId = other.parentId;
    }
}
