

flink-conf.yaml

```
metrics.reporters: es-logc
metrics.reporter.es-logc.class: me.samei.xtool.elasticsearch_metric_reporter.FlinkScheduledReporter
metrics.reporter.es-logc.index_prefix: test_v1
metrics.reporter.es-logc.index_age: weekly
metrics.reporter.es-logc.es_url: http://localhost:9200
```
