package db;


import java.util.ArrayList;
import java.util.List;

public class Node {

    public final Document doc;

    public final List<Node> children = new ArrayList<>();

    public Node(Document doc) {
        this.doc = doc;
    }

    public void addChildren(Node node) {
        children.add(node);
    }

    public boolean isRoot() {
        return doc == null;
    }
}
