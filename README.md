

![in use!](doc/image.png)

I found ElasticSearch (+ Kibana) an easy and flexible tool to use for monitoring my applications. 
When I started to use it for my Apache Flink jobs I wrote a simple reporter to send all metrics to ElasticSearch.
But unfortunately it wan't enough!

- Flink use identities like this for its metrics; for example consider this one:
`the_hostname.taskmanager.9893439894343_as_taskmangaer_id.my_application_name.332343_as_my_app_id....`
. This Id has a lot of changing parts! `the_hostname`, `9893439894343_as_taskmangaer_id`, ...! 
I know that I'm able to use scope configurations to change identity, but that didn't satisfied me.

- I needed to have metrics variables (`<host>`, `<job_name>`, ....) in my metrics result! Why? 
I want to aggregate reported metrics by filtering base on those values: consider the need to see 
all your application metrics (`<job_name>`) from a specific task manager (`<host>`); 
for this reason you should have those values in every document.

- I also need to seperate metrics per applications and other scopes! from the source. 
It will ended up to change metrics configuration per application! or maybe not.


So I improved my customized reporter to be configurable to fix my needs:

- being able to groups specific metrics together (as `GroupedMetrics` in source)
- being able to report every group to a different and dedicated resource (index and type in ElasticSearch)
- being able to add and track all variables in every report document (like `<host>`, `<tm>`, or additional vars like `<millis>`, etc.)
- take an eye on changes in metrics references
   
After that I generlized my reporter and to be used with Kamon too.

But anyway; here how it's work:

#### General Configuration

```yaml
metrics.reporter.{}.class: xyz.sigmalab.xtool.elasticreporter.v1.flink.Reporter
metrics.reporter.{}.name: metric.Application
metrics.reporter.{}.elastic-url: http://localhost:9200
metrics.reporter.{}.source-id: single-node
metrics.reporter.{}.index-pattern: flink-metrics-forapp-<job_name>-<year>-<month>
metrics.reporter.{}.id-pattern: <tm_id>-<task_name>-<job_name>-<operator_id>-<subtask_index>-<source_id>-<millis>
metrics.reporter.{}.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.{}.zone: UTC
```

- `metrics.reporter.{}.class: xyz.sigmalab.xtool.elasticreporter.v1.flink.Reporter`; The class that Flink wil instantiate as a reporter 
- `metrics.reporter.{}.name: metric.Application`; This will config a name that libraries internal logger will use it for this instance
- `metrics.reporter.{}.elastic-url: http://localhost:9200`; The URL that refers to ElasticSearch endpoint
- `metrics.reporter.{}.source-id: single-node`; The Id of this node; this could be different per JobManager & TaskManager instance (and it should be); I used this because the hostname wasn't engough,unique, and sometimes readable.
- `metrics.reporter.{}.index-pattern: flink-metrics-<job_name>-<year>-<month>` ; A pattern to make related index name per group! this pattern will be used with available variables in the metrics scopes and  some additional variables in the report context (timestamp, source-id, etc. keep reading to find full list of vars).
- `metrics.reporter.{}.id-pattern: <tm_id>-<task_name>-<job_name>-<operator_id>-<subtask_index>-<source_id>-<millis>`; same as for index, but this one will be used to generate the name of document-id 
- `metrics.reporter.{}.datetime-pattern: yyyy-MM-dd HH:mm:ss` the pattern to format datetime filed in result documents
- `metrics.reporter.{}.zone: UTC` the timezone to format datetime filed in result documents

# Application Specific Metrics
I use a specifc group name (`MetricGroup`) for all of my metrics over applications.
```scala
    lazy val metrics = getRuntime.getMetricGroup.addGroup("appmetric")
```
This will let met to filter related metrics easier. This is the configuration that I use to report application specific metrics:
```yaml
metrics.reporter.app.class: xyz.sigmalab.xtool.elasticreporter.v1.flink.Reporter
metrics.reporter.app.name: metric.Application
metrics.reporter.app.elastic-url: http://localhost:9200
metrics.reporter.app.source-id: single-node
metrics.reporter.app.index-pattern: flink-metrics-forapp-<job_name>-<year>-<month>
metrics.reporter.app.id-pattern: <tm_id>-<task_name>-<job_name>-<operator_id>-<subtask_index>-<source_id>-<millis>
metrics.reporter.app.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.app.zone: UTC
metrics.reporter.app.filter-by.select-scope: appmetric
metrics.reporter.app.group-by: <job_name>
metrics.reporter.app.name-by.select-scope: appmetric
```
- `metrics.reporter.app.filter-by.select-scope: appmetric`. I filter the metrics that have the mentioned scope in their scope list.
- `metrics.reporter.app.group-by: <job_name>`. Then I group filtered metrics base on the job that they relate to it (`<job_name>`, a variable provided by Flink itself)
- `metrics.reporter.app.name-by.select-scope: appmetric`. Then I assign a scoped name/Id to each metric in the group; in the result/report document, this id will be appeared

An example of inserted document in ElasticSearch: 
```json
{
  "_index": "flink-metrics-forapp-mysimple_testjob-2018-8",
  "_type": "doc",
  "_id": "8dad13fe8dcbaabd42f1f5ced28702be-triggerwindow-processingtimesessionwindows-120000-reducingstatedescriptor{serializer=org.apache.flink.api.java.typeutils.runtime.kryo.kryoserializer@1910163b-reducefunction=com.bisphone.calllog.writepath.mergelogevents-merge-}-com.bisphone.calllog.writepath.eventaggrigator$defaulttrigger@55787112-windowedstream.reduce-windowedstream.java-276-flat-map-map-map-calllog_aggr_rc1-operator_id-0-hadi.flink.tm.05-1534927371230",
  "_score": null,
  "_source": {
    "@meta.time.millis": 1534927371230,
    "@meta.uuid": "80a692dc-a63e-45df-831a-ccd2761a9795",
    "@meta.time.formatted": "2018-08-22 08:42:51",
    "@meta.source.id": "single-node",
    "even_numbs.count": 10362,
    "even_numbs.rate": 0.41000306771665294,
    "failures.count": 3,
    "failures.rate": 0.0001,
    "@meta.var.<job_id>": "b3f2321e76bce360734c1ffb459209c0",
    "@meta.var.<operator_name>": "Flat Map",
    "@meta.var.<task_name>": "TriggerWindow(ProcessingTimeSessionWindows(120000), ReducingStateDescriptor{...}) -> Flat Map -> Map -> Map",
    "@meta.var.<tm_id>": "8dad13fe8dcbaabd42f1f5ced28702be",
    "@meta.var.<millis>": "1534927371230",
    "@meta.var.<subtask_index>": "0",
    "@meta.var.<source_id>": "single-node",
    "@meta.var.<job_name>": "mysimple_testjob",
    "@meta.var.<host>": "localhost",
    "@meta.var.<month>": "8",
    "@meta.var.<day_of_month>": "22",
    "@meta.var.<year>": "2018",
    "@meta.var.<task_id>": "bf80c198166281d628838ffa871a5ca6",
    "@meta.var.<task_attempt_num>": "0",
    "@meta.var.<task_attempt_id>": "6baec68aebe4586978347448ba922e47"
  },
  "fields": {
    "@meta.time.millis": [
      1534927371230
    ]
  },
  "sort": [
    1534927371230
  ]
}
``` 



#### Metrics of JobManager & TaskManager

```yaml
# JobManager

metrics.reporter.jm.class: xyz.sigmalab.xtool.elasticreporter.v1.flink.Reporter
metrics.reporter.jm.name: metric.JM
metrics.reporter.jm.elastic-url: http://localhost:9200
metrics.reporter.jm.source-id: single-node
metrics.reporter.jm.index-pattern: flink-metrics-jobmanager-<year>-<month>
metrics.reporter.jm.id-pattern: <host>-<millis>
metrics.reporter.jm.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.jm.zone: UTC
metrics.reporter.jm.filter-by.match-scope: <host>.jobmanager
metrics.reporter.jm.filter-by.reject-vars: <job_name>.<tm_id>.<operator_name>
metrics.reporter.jm.group-by: <host>
metrics.reporter.jm.name-by.scope-drop-left: 2

# TaskManager
metrics.reporter.tm.class: xyz.sigmalab.xtool.elasticreporter.v1.flink.Reporter
metrics.reporter.tm.name: metric.TM
metrics.reporter.tm.elastic-url: http://localhost:9200
metrics.reporter.tm.source-id: single-node
metrics.reporter.tm.index-pattern: flink-metrics-taskmanager-<year>-<month>-<day_of_month>
metrics.reporter.tm.id-pattern: <host>-<millis>
metrics.reporter.tm.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.tm.zone: UTC
metrics.reporter.tm.filter-by.scope: <host>.taskmanager.<tm_id>
metrics.reporter.tm.filter-by.reject-vars: <job_name>
metrics.reporter.tm.group-by: <tm_id>
metrics.reporter.tm.name-by.scope-drop-left: 3
```

- `metrics.reporter.tm.filter-by.scope: <host>.taskmanager.<tm_id>`: Filtering based on the match scope pattern
- `metrics.reporter.jm.filter-by.reject-vars: <job_name>.<tm_id>.<operator_name>`: If the mentioned variabled/scopes apeared in MetricGroup, drop it! (these two filters will be applied sequentially)
- `metrics.reporter.tm.group-by: <tm_id>` or `metrics.reporter.jm.group-by: <host>`: This means that you will have one group per Reporter instance in every JobManager/TaskManagr
- `metrics.reporter.jm.name-by.scope-drop-left: 2`: A little weired! but this will transform `localhost.jobmanager.Stats.Network...` to `Stats.Network...`


##### Catch changes in metrics' references
This will log all added/removed meterics in configured index. 
In this way I'll be able to cache changes in metrics references
to consider changes in metrics/reporters configurations if needed.

```yaml

# Debugger
metrics.reporter.debug.class: xyz.sigmalab.xtool.elasticreporter.v1.flink.Debugger
metrics.reporter.debug.name: metric.Debugger
metrics.reporter.debug.elastic-url: http://localhost:9200
metrics.reporter.debug.source-id: single-node
metrics.reporter.debug.index-pattern: flink-mterics-debug-<year>-<month>
metrics.reporter.debug.id-pattern: <source_id>-<millis>-<uuid>
metrics.reporter.debug.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.debug.zone: UTC
```

#### All together

```yaml

metrics.reporters: debug, jm, tm, jobstat, jobtask, joboperator 

# Debugger
metrics.reporter.debug.class: xyz.sigmalab.xtool.elasticreporter.v1.flink.Debugger
metrics.reporter.debug.name: metric.Debugger
metrics.reporter.debug.elastic-url: http://localhost:9200
metrics.reporter.debug.source-id: single-node
metrics.reporter.debug.index-pattern: flink-mterics-debug-<year>-<month>
metrics.reporter.debug.id-pattern: <source_id>-<millis>-<uuid>
metrics.reporter.debug.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.debug.zone: UTC

# Application
metrics.reporter.app.class: xyz.sigmalab.xtool.elasticreporter.v1.flink.Reporter
metrics.reporter.app.name: metric.Application
metrics.reporter.app.elastic-url: http://localhost:9200
metrics.reporter.app.source-id: single-node
metrics.reporter.app.index-pattern: flink-metrics-forapp-<job_name>-<year>-<month>
metrics.reporter.app.id-pattern: <tm_id>-<task_name>-<job_name>-<operator_id>-<subtask_index>-<source_id>-<millis>
metrics.reporter.app.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.app.zone: UTC
metrics.reporter.app.filter-by.select-scope: appmetric
metrics.reporter.app.group-by: <job_name>
metrics.reporter.app.name-by.select-scope: appmetric

# JobManager
metrics.reporter.jm.class: xyz.sigmalab.xtool.elasticreporter.v1.flink.Reporter
metrics.reporter.jm.name: metric.JM
metrics.reporter.jm.elastic-url: http://localhost:9200
metrics.reporter.jm.source-id: single-node
metrics.reporter.jm.index-pattern: flink-metrics-jobmanager-<year>-<month>
metrics.reporter.jm.id-pattern: <host>-<millis>
metrics.reporter.jm.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.jm.zone: UTC
metrics.reporter.jm.filter-by.match-scope: <host>.jobmanager
metrics.reporter.jm.filter-by.reject-vars: <job_name>.<tm_id>.<operator_name>
metrics.reporter.jm.group-by: <host>
metrics.reporter.jm.name-by.scope-drop-left: 2

# TaskManager
metrics.reporter.tm.class: xyz.sigmalab.xtool.elasticreporter.v1.flink.Reporter
metrics.reporter.tm.name: metric.TM
metrics.reporter.tm.elastic-url: http://localhost:9200
metrics.reporter.tm.source-id: single-node
metrics.reporter.tm.index-pattern: flink-metrics-taskmanager-<year>-<month>-<day_of_month>
metrics.reporter.tm.id-pattern: <host>-<millis>
metrics.reporter.tm.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.tm.zone: UTC
metrics.reporter.tm.filter-by.scope: <host>.taskmanager.<tm_id>
metrics.reporter.tm.filter-by.reject-vars: <job_name>
metrics.reporter.tm.group-by: <tm_id>
metrics.reporter.tm.name-by.scope-drop-left: 3

# ====================================================================================

# Jobs Stats
metrics.reporter.jobstat.class: xyz.sigmalab.xtool.elasticreporter.v1.flink.Reporter
metrics.reporter.jobstat.name: metric.JobState
metrics.reporter.jobstat.elastic-url: http://localhost:9200
metrics.reporter.jobstat.source-id: single-node
metrics.reporter.jobstat.index-pattern: flink-metrics-jobstat-<job_name>-at-<year>-<month>-<day_of_month>
metrics.reporter.jobstat.id-pattern: <task_id>-<subtask_index>-<source_id>-<millis>
metrics.reporter.jobstat.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.jobstat.zone: UTC
metrics.reporter.jobstat.filter-by.scope: <host>.jobmanager.<job_name>
metrics.reporter.jobstat.filter-by.reject-vars: <tm_id>.<operator_id>
metrics.reporter.jobstat.group-by: <job_name>
metrics.reporter.jobstat.name-by.scope-drop-left: 3

# Task Metrics Per Job
metrics.reporter.jobtask.class: xyz.sigmalab.xtool.elasticreporter.v1.flink.Reporter
metrics.reporter.jobtask.name: metric.TaskOfJob
metrics.reporter.jobtask.elastic-url: http://localhost:9200
metrics.reporter.jobtask.source-id: single-node
metrics.reporter.jobtask.index-pattern: flink-metrics-forjob-<job_name>-at-<year>-<month>-<day_of_month>
metrics.reporter.jobtask.id-pattern: <task_id>-<subtask_index>-<source_id>-<millis>
metrics.reporter.jobtask.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.jobtask.zone: UTC
metrics.reporter.jobtask.filter-by.scope: <host>.taskmanager.<tm_id>.<job_name>.<task_name>.<subtask_index>
metrics.reporter.jobtask.group-by: <job_name>-<task_id>-<subtask_index>
metrics.reporter.jobtask.name-by.scope-drop-left: 6

# Operator Metrics Per Job

metrics.reporter.joboperator.class: com.sameei.xtool.elasticreporter.v1.flink.Reporter
metrics.reporter.joboperator.name: metric.OpteratorOfJob
metrics.reporter.joboperator.elastic-url: http://localhost:9200
metrics.reporter.joboperator.source-id: single-node
metrics.reporter.joboperator.index-pattern: flink-metrics-foroperator-<job_name>-<operator_name>-<year>-<month>-<day_of_month>
metrics.reporter.joboperator.id-pattern: <operator_id>-<subtask_index>-<source_id>-<millis>
metrics.reporter.joboperator.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.joboperator.zone: UTC
metrics.reporter.joboperator.filter-by.scope: <host>.taskmanager.<tm_id>.<job_name>.<operator_name>.<subtask_index>
metrics.reporter.joboperator.group-by: <job_name>-<operator_id>-<subtask_index>
metrics.reporter.joboperator.name-by.scope-drop-left: 6

# ====================================================================================

# metrics.reporter.{}.name-by.selected-scope = developerdefined
# metrics.reporter.{}.filter-by.selected-scope = developerdefined

```

##### Available Variables
- Availabe in all reports : `<year>`, `<month>`, `<day_of_month>`, `<millis>`, `<source_id>`, `<uuid>`
- Provided by Flink Metric: https://ci.apache.org/projects/flink/flink-docs-stable/monitoring/metrics.html#user-variables

## Use with Kamon

```scala
val reporter = xyz.sigmalab.xtool.elasticreporter.v1.kamon.Reporter.fromConfig(typesafeConfig, "app.reporter.config")
kamon.Kamon.addReporter(reporter)
```


```hocon
app.reporter.config = {
  name = "kamon.elastic-reporter"
  source-id = "SimpleAkkaApp"
  elastic-url = "http://localhost:9200"
  index-pattern = "<source_id>-<year>-<month>-<day_of_month>"
  id-pattern = "<source_id>-<millis>"
  datetime-pattern = "yyyy-MM-dd HH:mm:ss"
  datetime-zone = "UTC"
}
```


