package hello.advanced.trace;

import java.util.UUID;

public class TraceId {
    private static final int FIRST_LEVEL = 0;
    private final String id;
    private final int level;

    public TraceId() {
        this. id = createId();
        this.level = FIRST_LEVEL;
    }

    private TraceId(String id, int level) {
        this.id = id;
        this.level = level;
    }

    private String createId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public TraceId createNextId() {
        return new TraceId(id, level + 1);
    }

    public TraceId createPreviousId() {
        return new TraceId(id, level - 1);
    }

    public boolean isFirstLevel() {
        return this.level == FIRST_LEVEL;
    }

    public int getLevel() {
        return level;
    }

    public String getId() {
        return id;
    }
}
