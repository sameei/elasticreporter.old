package xyz.sigmalab.xtool.elasticreporter.v1.flink.lego

import java.util

import xyz.sigmalab.xtool.elasticreporter.v1.flink.lego.data.FakeMetricRef
import org.apache.flink.metrics._
import org.scalatest._

class FilterBySuite extends FlatSpec with Matchers {

    /*
    2018-05-22 12:35:18,282 DEBUG
    com.sameei.xtool.elasticreporter.v1.flink.lego.FilterBy$Scope  -
    Filter,
    CurrentScope: localhost.taskmanager.ca75e2f0b9f4bfee9b08f2fc6f4fe3b7.Status.Network,
    RequiredScope: localhost.taskmanager.ca75e2f0b9f4bfee9b08f2fc6f4fe3b7,
    Scope: List(localhost, taskmanager, ca75e2f0b9f4bfee9b08f2fc6f4fe3b7, Status, Network),
    Vars: Map(<host> -> localhost, <tm_id> -> ca75e2f0b9f4bfee9b08f2fc6f4fe3b7)
    */

    "MatchScope" must "accept" in {

        FilterBy.MatchScope("?","<host>.taskmanager").drop(FakeMetricRef(
            "AvailableMemorySegments",
            "localhost.taskmanager.ca75e2f0b9f4bfee9b08f2fc6f4fe3b7.Status.Network.AvailableMemorySegments",
            Map("<host>" -> "localhost", "<tm_id>" -> "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7"),
            List("localhost", "taskmanager", "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7", "Status", "Network")
        )) shouldEqual false

        FilterBy.MatchScope("?","<host>.taskmanager.<tm_id>").drop(FakeMetricRef(
            "AvailableMemorySegments",
            "localhost.taskmanager.ca75e2f0b9f4bfee9b08f2fc6f4fe3b7.Status.Network.AvailableMemorySegments",
            Map("<host>" -> "localhost", "<tm_id>" -> "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7"),
            List("localhost", "taskmanager", "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7", "Status", "Network")
        )) shouldEqual false

    }

    "Scope" must "drop" in {

        FilterBy.MatchScope("?","<host>.taskmanager.<tm_id>.something").drop(FakeMetricRef(
            "AvailableMemorySegments",
            "localhost.taskmanager.ca75e2f0b9f4bfee9b08f2fc6f4fe3b7.Status.Network.AvailableMemorySegments",
            Map("<host>" -> "localhost", "<tm_id>" -> "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7"),
            List("localhost", "taskmanager", "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7", "Status", "Network")
        )) shouldEqual true
    }

    "RejectVars" must "accept" in {

        FilterBy.RejectVars("?","<job_name>").drop(FakeMetricRef(
            "AvailableMemorySegments",
            "localhost.taskmanager.ca75e2f0b9f4bfee9b08f2fc6f4fe3b7.Status.Network.AvailableMemorySegments",
            Map("<host>" -> "localhost", "<tm_id>" -> "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7"),
            List("localhost", "taskmanager", "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7", "Status", "Network")
        )) shouldEqual false

    }

    "RejectVars" must "drop" in {

        FilterBy.RejectVars("?","<job_name>.<tm_id>").drop(FakeMetricRef(
            "AvailableMemorySegments",
            "localhost.taskmanager.ca75e2f0b9f4bfee9b08f2fc6f4fe3b7.Status.Network.AvailableMemorySegments",
            Map("<host>" -> "localhost", "<tm_id>" -> "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7"),
            List("localhost", "taskmanager", "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7", "Status", "Network")
        )) shouldEqual true

        FilterBy.RejectVars("?","<job_name>").drop(FakeMetricRef(
            "AvailableMemorySegments",
            "localhost.taskmanager.ca75e2f0b9f4bfee9b08f2fc6f4fe3b7.Status.Network.AvailableMemorySegments",
            Map("<host>" -> "localhost", "<tm_id>" -> "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7"),
            List("localhost", "taskmanager", "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7", "Status", "Network")
        )) shouldEqual false

    }

    "ForceVars" must "accept" in {

        FilterBy.ForceVars("?","<host>.<tm_id>").drop(FakeMetricRef(
            "AvailableMemorySegments",
            "localhost.taskmanager.ca75e2f0b9f4bfee9b08f2fc6f4fe3b7.Status.Network.AvailableMemorySegments",
            Map("<host>" -> "localhost", "<tm_id>" -> "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7"),
            List("localhost", "taskmanager", "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7", "Status", "Network")
        )) shouldEqual false

        FilterBy.ForceVars("?","<host>.<tm_id>").drop(FakeMetricRef(
            "AvailableMemorySegments",
            "localhost.taskmanager.ca75e2f0b9f4bfee9b08f2fc6f4fe3b7.Status.Network.AvailableMemorySegments",
            Map("<tm_id>" -> "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7"),
            List("localhost", "taskmanager", "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7", "Status", "Network")
        )) shouldEqual true

    }

    "Seq" must "accept & drop" in {

        val fake = FakeMetricRef(
            "AvailableMemorySegments",
            "localhost.taskmanager.ca75e2f0b9f4bfee9b08f2fc6f4fe3b7.Status.Network.AvailableMemorySegments",
            Map("<host>" -> "localhost", "<tm_id>" -> "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7"),
            List("localhost", "taskmanager", "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7", "Status", "Network")
        )


        val filters = FilterBy.ForceVars("?","<tm_id>") ::
            FilterBy.RejectVars("?","<job_name>") ::
            FilterBy.MatchScope("?","<host>.taskmanager.<tm_id>") :: Nil

        FilterBy(filters, FakeMetricRef(
            "AvailableMemorySegments",
            "localhost.taskmanager.ca75e2f0b9f4bfee9b08f2fc6f4fe3b7.Status.Network.AvailableMemorySegments",
            Map("<host>" -> "localhost", "<tm_id>" -> "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7"),
            List("localhost", "taskmanager", "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7", "Status", "Network")
        )).isDefined shouldEqual false

        {
            val filter = FilterBy(filters, FakeMetricRef(
                "AvailableMemorySegments",
                "localhost.taskmanager.ca75e2f0b9f4bfee9b08f2fc6f4fe3b7.Status.Network.AvailableMemorySegments",
                Map("<host>" -> "localhost", "<tm_id>" -> "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7", "<job_name>" -> "???"),
                List("localhost", "taskmanager", "ca75e2f0b9f4bfee9b08f2fc6f4fe3b7", "Status", "Network")
            ))

            filter.isDefined shouldEqual true
            filter.get shouldBe a[FilterBy.RejectVars]
        }
    }

    "Seq2" must "?" in {

        /*
        2018-05-22 14:15:04,390 INFO
        com.sameei.xtool.elasticreporter.v1.flink.Reporter.reporter   -
        Select,
        GroupID: localhost,
        MetricKey: y21.downtime,
        Metric: org.apache.flink.runtime.executiongraph.metrics.DownTimeGauge,
        Name: downtime,
        Id: localhost.jobmanager.y21.downtime,
        Vars: Map(
            <job_id> -> c0a6056dfc7d79540eae4f8f362a4413,
            <host> -> localhost,
            <job_name> -> y21
        ),
        Scope: List(
            localhost,
            jobmanager,
            y21
        )
        */


        val fake = FakeMetricRef(
            "downtime",
            "localhost.jobmanager.y21.downtime",
            Map(
                "<job_id>" -> "c0a6056dfc7d79540eae4f8f362a4413",
                "<host>" -> "localhost",
                "<job_name>" -> "y21"
            ),
            Seq("localhost", "jobmanager", "y21")
        )

        val filters = Seq(
            FilterBy.RejectVars("?","<job_name>"),
            FilterBy.MatchScope("?","<host>.jobmanager")
        )


        val rsl = FilterBy(filters, fake)

        rsl.isDefined
        rsl.get shouldBe a[FilterBy.RejectVars]
    }

    "Seq3" must "?" in {

        /*

        2018-05-22 14:15:07,332
        DEBUG
        com.sameei.xtool.elasticreporter.v1.flink.lego.FilterBy$Scope
        -
        Filter,
        CurrentScope: localhost.taskmanager.1e29235bfe186bbf63eaff66265900fe.y21.Combine(groupby-and-sum).0.usergroup,
        RequiredScope: localhost.jobmanager, ID: localhost.taskmanager.1e29235bfe186bbf63eaff66265900fe.y21.Combine(groupby-and-sum).0.usergroup.count,
        Scope: List(
            localhost,
            taskmanager,
            1e29235bfe186bbf63eaff66265900fe,
            y21,
            Combine(groupby-and-sum),
            0,
            usergroup
        ),
        Vars: Map(
            <job_id> -> c0a6056dfc7d79540eae4f8f362a4413,
            <operator_name> -> Combine(groupby-and-sum),
            <task_name> -> CHAIN DataSource (at org.apache.flink.api.scala.ExecutionEnvironment.fromElements(ExecutionEnvironment.scala:455) (org.apache.flink.api.java.io.CollectionInputFormat)) -> FlatMap (FlatMap at org.example.WordCount$.main(WordCount.scala:56)) -> Map (proxy-counter) -> Map (count-from-one) -> Combine(groupby-and-sum),
            <tm_id> -> 1e29235bfe186bbf63eaff66265900fe,
            <subtask_index> -> 0,
            <job_name> -> y21,
            <host> -> localhost,
            <task_id> -> ebb678742a249e033f3e45348487f85e,
            <operator_id> -> ebb678742a249e033f3e45348487f85e,
            <task_attempt_num> -> 0,
            <task_attempt_id> -> 7fe917c164ccfdd0d326e75dead3121a
        )






        2018-05-22 14:34:17,543 DEBUG com.sameei.xtool.elasticreporter.v1.flink.lego.FilterBy$Scope  -
        Filter,
        CurrentScope: localhost.taskmanager.668ea12273f8f4d0bb8d394def432fcf.y23.Combine(groupby-and-sum).0.usergroup,
        RequiredScope: localhost.taskmanager.668ea12273f8f4d0bb8d394def432fcf.y23.CHAIN DataSource (at org.apache.flink.api.scala.ExecutionEnvironment.fromElements(ExecutionEnvironment.scala:455) (org.apache.flink.api.java.io.CollectionInputFormat)) -> FlatMap (FlatMap at org.example.WordCount$.main(WordCount.scala:56)) -> Map (proxy-counter) -> Map (count-from-one) -> Combine(groupby-and-sum).0, ID: localhost.taskmanager.668ea12273f8f4d0bb8d394def432fcf.y23.Combine(groupby-and-sum).0.usergroup.count, Scope: List(localhost, taskmanager, 668ea12273f8f4d0bb8d394def432fcf, y23, Combine(groupby-and-sum), 0, usergroup), Vars: Map(<job_id> -> ae564341ba9b8ff87f1de5d7f2c2dcec, <operator_name> -> Combine(groupby-and-sum), <task_name> -> CHAIN DataSource (at org.apache.flink.api.scala.ExecutionEnvironment.fromElements(ExecutionEnvironment.scala:455) (org.apache.flink.api.java.io.CollectionInputFormat)) -> FlatMap (FlatMap at org.example.WordCount$.main(WordCount.scala:56)) -> Map (proxy-counter) -> Map (count-from-one) -> Combine(groupby-and-sum), <tm_id> -> 668ea12273f8f4d0bb8d394def432fcf, <subtask_index> -> 0, <job_name> -> y23, <host> -> localhost, <task_id> -> 60d7b4ca0dde89dd27794e1a941f7007, <operator_id> -> 60d7b4ca0dde89dd27794e1a941f7007, <task_attempt_num> -> 0, <task_attempt_id> -> 2a11ab27b982885362676780b990e8bb)

        2018-05-22 14:34:17,543 INFO  com.sameei.xtool.elasticreporter.v1.flink.Reporter.reporter   - Filter, Scope(<host>.taskmanager.<tm_id>.<job_name>.<task_name>.<subtask_index>), Metric: org.apache.flink.metrics.SimpleCounter, Name: count, Id: localhost.taskmanager.668ea12273f8f4d0bb8d394def432fcf.y23.Combine(groupby-and-sum).0.usergroup.count, Vars: Map(<job_id> -> ae564341ba9b8ff87f1de5d7f2c2dcec, <operator_name> -> Combine(groupby-and-sum), <task_name> -> CHAIN DataSource (at org.apache.flink.api.scala.ExecutionEnvironment.fromElements(ExecutionEnvironment.scala:455) (org.apache.flink.api.java.io.CollectionInputFormat)) -> FlatMap (FlatMap at org.example.WordCount$.main(WordCount.scala:56)) -> Map (proxy-counter) -> Map (count-from-one) -> Combine(groupby-and-sum), <tm_id> -> 668ea12273f8f4d0bb8d394def432fcf, <subtask_index> -> 0, <job_name> -> y23, <host> -> localhost, <task_id> -> 60d7b4ca0dde89dd27794e1a941f7007, <operator_id> -> 60d7b4ca0dde89dd27794e1a941f7007, <task_attempt_num> -> 0, <task_attempt_id> -> 2a11ab27b982885362676780b990e8bb), Scope: List(localhost, taskmanager, 668ea12273f8f4d0bb8d394def432fcf, y23, Combine(groupby-and-sum), 0, usergroup), Map(<job_id> -> ae564341ba9b8ff87f1de5d7f2c2dcec, <operator_name> -> Combine(groupby-and-sum), <task_name> -> CHAIN DataSource (at org.apache.flink.api.scala.ExecutionEnvironment.fromElements(ExecutionEnvironment.scala:455) (org.apache.flink.api.java.io.CollectionInputFormat)) -> FlatMap (FlatMap at org.example.WordCount$.main(WordCount.scala:56)) -> Map (proxy-counter) -> Map (count-from-one) -> Combine(groupby-and-sum), <tm_id> -> 668ea12273f8f4d0bb8d394def432fcf, <subtask_index> -> 0, <job_name> -> y23, <host> -> localhost, <task_id> -> 60d7b4ca0dde89dd27794e1a941f7007, <operator_id> -> 60d7b4ca0dde89dd27794e1a941f7007, <task_attempt_num> -> 0, <task_attempt_id> -> 2a11ab27b982885362676780b990e8bb), List(localhost, taskmanager, 668ea12273f8f4d0bb8d394def432fcf, y23, Combine(groupby-and-sum), 0, usergroup)






        */


    }


}