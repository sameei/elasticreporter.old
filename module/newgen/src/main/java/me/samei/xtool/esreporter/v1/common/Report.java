package me.samei.xtool.esreporter.v1.common;

public class Report {

    public final String index;
    public final String body;
    public final long time;

    public Report(long time, String index, String body) {
        this.time = time;
        this.index = index;
        this.body = body;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(getClass().getName())
                .append("( time: ").append(time)
                .append(", index: '").append(index)
                .append("', body: '").append(body)
                .append("')").toString();
    }
}
