package com.sameei.xtool.elasticreporter.v1.flink.reporter

import com.sameei.xtool.elasticreporter.v1.flink.lego.ReporterForJob
import org.apache.flink.metrics.{Metric, MetricGroup}
import scala.collection.JavaConverters._

object Job {

    class ByTask extends ReporterForJob {

        val JobName = "<job_name>"
        val JobId = "<job_id>"
        val TaskName = "<task_name>"
        val TaskId = "<task_id>"
        val SubtaskIndex = "<subtask_index>"

        protected override val keys = Array(JobName, JobName, TaskName, TaskId, SubtaskIndex)

        override protected def toGroupId(name : String, metric : Metric, group : MetricGroup) : String = {
            val vars = group.getAllVariables.asScala
            s"${vars(JobName)}.${vars(TaskId)}.${vars(SubtaskIndex)}"
        }

        override protected def toMetricName(name : String, metric : Metric, group : MetricGroup) : String = {

            // @Consider
            // This depends to default value of 'metrics.scope.task' in config
            // metrics.scope.task: <host>.taskmanager.<tm_id>.<job_name>.<task_name>.<subtask_index>
            // @todo document this!

            val limit = 6

            if (group.getScopeComponents.size < limit) {
                logger.warn(
                    s"""
                         |MetricName,
                         |Invalid Scope: [${group.getScopeComponents.mkString(",")}],
                         |Less than ${limit},
                         |Return Name as MetricName: ${name},
                         |Desc: It seems that you have change the defualt value of 'metrics.scope.task';
                         |this component(${getClass.getName}) needs it be default value:
                         |<host>.taskmanager.<tm_id>.<job_name>.<task_name>.<subtask_index>
                         |""".stripMargin.replace("\n", " ")
                )
                name
            } else if (group.getScopeComponents.size == limit) {
                name
            } else {
                s"${group.getScopeComponents.drop(limit).mkString(".")}.${name}"
            }
        }
    }

}
