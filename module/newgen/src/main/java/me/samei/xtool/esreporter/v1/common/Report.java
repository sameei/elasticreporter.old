package me.samei.xtool.esreporter.v1.common;

public class Report {

    public final long time;
    public final String index;
    public final String id;
    public final String body;

    public Report(long time, String index, String id, String body) {
        this.time = time;
        this.index = index;
        this.id = id;
        this.body = body;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(getClass().getName())
                .append("( time: ").append(time)
                .append(", index: ").append(index)
                .append(", id: ").append(id)
                .append(", body: '").append(body)
                .append("')").toString();
    }
}
