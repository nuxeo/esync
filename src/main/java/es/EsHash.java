package es;

public class EsHash {
    private static final String DOC_TYPE = "doc";
    private static final int DEFAULT_SHARD = 5;

    public static int djbHash(String type, String id) {
        long hash = 5381;
        for (int i = 0; i < type.length(); i++) {
            hash = ((hash << 5) + hash) + type.charAt(i);
        }
        for (int i = 0; i < id.length(); i++) {
            hash = ((hash << 5) + hash) + id.charAt(i);
        }
        return (int) hash;
    }

    public static int simpleHash(String type, String id) {
        return type.hashCode() + 31 * id.hashCode();
    }

    public static int getShard(String id, int nbShard) {
        return simpleHash(DOC_TYPE, id) % nbShard;
    }

    /**
     * Return the shard number where the doc id is indexed
     */
    public static int getShard(String id) {
        return getShard(id, DEFAULT_SHARD);
    }
}
