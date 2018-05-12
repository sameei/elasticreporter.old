package me.samei.xtool.esreporter.v1.flink;

public class BulkReporter extends AbstractReporter {

    private GroupedMetrics _metrics = new GroupedMetrics();

    @Override
    protected GroupedMetrics metrics() { return _metrics; }

    @Override
    protected String name() { return "All"; }


    private Select _select = new Select.AcceptAll();

    @Override
    protected Select select() { return _select; }


}

