package db;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

public class Document {
    public final String id;
    public String[] acl;
    public String path;
    public String parentId;

    static public final String[] NO_ACL = {};

    public Document(String id, String acl[], String path) {
        this.id = id;
        this.acl = acl;
        this.path = path;
    }

    public Document(String id, String acl[]) {
        this.id = id;
        this.acl = acl;
    }

    @Override
    public String toString() {
        return String.format("<doc id=%s acl=%s path=%s parentid=%s />", id,
                StringUtils.join(acl, ","), path, parentId);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof Document)) {
            return false;
        }
        Document other = (Document) obj;
        if (id != null && !id.equals(other.id)) {
            return false;
        }
        if (acl != null && !Arrays.equals(acl, other.acl)) {
            return false;
        }
        if (path != null && !path.equals(other.path)) {
            return false;
        }
        return !(parentId != null && !parentId.equals(other.parentId));
    }

    public void merge(Document other) {
        this.acl = other.acl;
        this.path = other.path;
        this.parentId = other.parentId;
    }
}
