package com.sameei.xtool.elasticreporter.v1.flink.reporter

import com.sameei.xtool.elasticreporter.v1.flink.lego
import com.sameei.xtool.elasticreporter.v1.flink.lego.ReporterForMultipleGroups
import org.apache.flink.metrics.{Metric, MetricGroup}
import scala.collection.JavaConverters._

class JobTask extends lego.ReporterForMultipleGroups {

    val JobName = "<job_name>"
    val JobId = "<job_id>"
    val TaskName = "<task_name>"
    val TaskId = "<task_id>"
    val SubtaskIndex = "<subtask_index>"

    protected val keys = Array(JobName, JobName, TaskName, TaskId, SubtaskIndex)

    protected val filterKeys = Array(
        "<host>", "<tm_id>", "<job_id>", "<job_name>",
        "<task_id>", "<task_name>", "<task_attempt_id>", "<task_attempt_num>", "<subtask_index>",
        "<operator_id>", "<operator_name>"
    )

    override protected def select(
        name : String, metric : Metric, group : MetricGroup
    ) : Option[ReporterForMultipleGroups.Ref] = {
        val vars = group.getAllVariables.asScala

        if (vars.size < keys.size) None
        else {

            val count = keys.foldLeft(0) { (count, k) =>
                if (vars.contains(k)) count + 1
                else count
            }

            if (count < keys.size) None
            else {
                val groupdId = s"${vars(JobName)}.${vars(TaskId)}.${vars(SubtaskIndex)}"
                group.getScopeComponents
                val metricKey = s"${name}."
                ???
            }
        }
    }
}
