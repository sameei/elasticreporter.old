package com.sameei.xtool.elasticreporter.v1.flink.reporter

import com.sameei.xtool.elasticreporter.v1.flink.lego.ReporterForJob
import com.sameei.xtool.elasticreporter.v1.flink.lego.data.MetricRef
import org.apache.flink.metrics.{Metric, MetricGroup}

import scala.collection.JavaConverters._

object Job {

    /**
      * metrics.scope.task
      * ```<host>.taskmanager.<tm_id>.<job_name>.<task_name>.<subtask_index>```
      *
      */
    class ByTask extends ReporterForJob {

        val JobName = "<job_name>"
        val JobId = "<job_id>"
        val TaskName = "<task_name>"
        val TaskId = "<task_id>"
        val SubtaskIndex = "<subtask_index>"

        protected override val keys = Array(JobName, JobName, TaskName, TaskId, SubtaskIndex)

        override protected def toGroupId(ref: MetricRef) : String = {
            val vars = ref.group.getAllVariables.asScala
            s"${vars(JobName)}.${vars(TaskId)}.${vars(SubtaskIndex)}"
        }

        override protected def toMetricName(ref: MetricRef) : String = {

            val limit = 6

            val scopes = ref.group.getScopeComponents

            if (scopes.size < limit) {
                logger.warn(
                    s"""
                         |MetricName,
                         |Invalid Scope: [${ref.group.getScopeComponents.mkString(",")}],
                         |Less than ${limit},
                         |Return Name as MetricName: ${name},
                         |Desc: It seems that you have change the defualt value of 'metrics.scope.task';
                         |this component(${getClass.getName}) needs it be default value:
                         |<host>.taskmanager.<tm_id>.<job_name>.<task_name>.<subtask_index>
                         |""".stripMargin.replace("\n", " ")
                )
                name
            } else if (scopes.size == limit) {
                name
            } else {
                s"${scopes.drop(limit).mkString(".")}.${name}"
            }
        }
    }

    /**
      *
      * metrics.scope.operator
      * ```<host>.taskmanager.<tm_id>.<job_name>.<operator_name>.<subtask_index>```
      *
      */
    class ByOperator extends ReporterForJob {

        val JobName = "<job_name>"
        val JobId = "<job_id>"
        val OperatorName = "<operator_name>"
        val OperatorId = "<operator_id>"
        val SubtaskIndex = "<subtask_index>"

        protected override val keys = Array(JobName, JobName, OperatorName, OperatorId, SubtaskIndex)

        override protected def toGroupId(ref: MetricRef) : String = {
            val vars = ref.group.getAllVariables.asScala
            s"${vars(JobName)}.${vars(OperatorId)}.${vars(SubtaskIndex)}"
        }

        override protected def toMetricName(ref: MetricRef) : String = {

            val limit = 6

            val scopes = ref.group.getScopeComponents

            if (scopes.size < limit) {
                logger.warn(
                    s"""
                         |MetricName,
                         |Invalid Scope: [${scopes.mkString(",")}],
                         |Less than ${limit},
                         |Return Name as MetricName: ${name},
                         |Desc: It seems that you have change the defualt value of 'metrics.scope.operator';
                         |this component(${getClass.getName}) needs it be default value:
                         |<host>.taskmanager.<tm_id>.<job_name>.<operator_name>.<subtask_index>
                         |""".stripMargin.replace("\n", " ")
                )
                name
            } else if (scopes.size == limit) {
                name
            } else {
                s"${scopes.drop(limit).mkString(".")}.${name}"
            }
        }

    }

}
