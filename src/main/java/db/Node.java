package db;

import java.util.ArrayList;
import java.util.List;

public class Node {

    public final Document doc;

    public List<Node> children = new ArrayList<Node>();

    public Node(Document doc) {
        this.doc = doc;
    }

    public void addChildren(Node node) {
        children.add(node);
    }
}
