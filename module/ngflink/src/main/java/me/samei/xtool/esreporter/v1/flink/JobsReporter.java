package me.samei.xtool.esreporter.v1.flink;

public class JobsReporter extends AbstractReporter {

    private static String[] _tokens = {
            "taskSlotsAvailable",
            "taskSlotsTotal",
            "numRegisteredTaskManagers",
            "numRunningJobs",
    };

    @Override
    protected String name() { return "Jobs"; }


    private Select _select = new Select.MatchByEnd(_tokens, name());

    @Override
    protected Select select() { return _select; }
}
