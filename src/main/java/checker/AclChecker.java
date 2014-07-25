package checker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import com.google.common.eventbus.EventBus;
import event.DiffEvent;
import event.InfoEvent;
import event.MissingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ESyncConfig;
import db.Document;
import db.Node;

public class AclChecker extends AbstractChecker {

    private static final Logger log = LoggerFactory.getLogger(AclChecker.class);

    public AclChecker(ESyncConfig config, EventBus eventBus) {
        super(config, eventBus);
    }

    @Override
    void check() {
        List<Document> docsWithAcl = db.getDocumentWithAcl();
        int aclDocumentCount = docsWithAcl.size();
        post(new InfoEvent(String.format("%d documents hold an ACL", aclDocumentCount)));
        compareWithEs(docsWithAcl);
        Node root = buildTree(docsWithAcl);
        printTree(root, 0);
        checkAclConsistencyRecursive(root);
    }

    private void checkAclConsistencyRecursive(Node root) {
        Queue<Node> queue = new LinkedList<>();
        queue.add(root);
        do {
            Node node = queue.poll();
            checkAclConsistency(node);
            queue.addAll(node.children);
        } while (!queue.isEmpty());
    }

    private void checkAclConsistency(Node node) {
        if (node.isRoot()) {
            return;
        }
        String path = node.doc.path;
        String[] acl = node.doc.acl;
        List<String> excludePath = new ArrayList<>();
        for (Node child : node.children) {
            excludePath.add(child.doc.path);
        }
        es.checkSameAcl(acl, path, excludePath);
    }

    private Node buildTree(List<Document> documents) {
        sortDocumentsByPath(documents);
        List<Node> nodes = new ArrayList<>();
        Node root = new Node(null);
        for (Document doc : documents) {
            Node parent = null;
            Node node = new Node(doc);
            String path = doc.path;
            // a parent of a document is on the top of the list
            for (Node potentialAncestor : nodes) {
                String ancestorPath = potentialAncestor.doc.path;
                if (path.startsWith(ancestorPath)) {
                    // the one with the longest path is the direct parent
                    if (parent == null
                            || ancestorPath.length() > parent.doc.path.length()) {
                        parent = potentialAncestor;
                    }
                }
            }
            if (parent == null) {
                root.addChildren(node);
            } else {
                parent.addChildren(node);
            }
            nodes.add(node);
        }
        return root;
    }

    private void sortDocumentsByPath(List<Document> docsWithAcl) {
        Collections.sort(docsWithAcl, new Comparator<Document>() {
            @Override
            public int compare(Document a, Document b) {
                return a.path.compareTo(b.path == null ? "NULL" : b.path);
            }
        });
    }

    private void compareWithEs(List<Document> documents) {
        Document esDoc;
        for (Document doc : documents) {
            log.debug(doc.toString());
            try {
                esDoc = es.getDocument(doc.id);
            } catch (NoSuchElementException e) {
                post(new MissingEvent(doc, "not found in es"));
                continue;
            }
            if (!doc.equals(esDoc)) {
                post(new DiffEvent(doc, esDoc, "ACL diff found"));
            }
            doc.merge(esDoc);
        }
    }

    private void printTree(Node parent, int depth) {
        if (parent.isRoot()) {
            System.out.println("ROOT");
        } else {
            for (int i = 0; i < depth; i++) {
                System.out.print("  ");
            }
            System.out.println(parent.doc.path + " " + parent.doc);
        }
        depth++;
        for (Node child : parent.children) {
            printTree(child, depth);
        }
        depth--;
    }
}
