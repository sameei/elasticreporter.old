package org.example

import java.util.concurrent.atomic.AtomicBoolean

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.client.program.{ClusterClient, JobWithJars}
import org.apache.flink.client.program.rest.RestClusterClient
import org.apache.flink.configuration.Configuration
import org.apache.flink.dropwizard.metrics.DropwizardMeterWrapper
import org.apache.flink.metrics.{Meter, MetricGroup}
import org.apache.flink.streaming.api._
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._ // createTypeInformation

object StreamTest {

    class FakeSource[T](fn: (Long) => T, delay: Long) extends SourceFunction[T] { self =>

        private val continue = new AtomicBoolean(true)

        override def run(ctx : SourceFunction.SourceContext[T]) : Unit = {
            val thread = new Thread(new Runnable {
                override def run() : Unit = while(self.continue.get) {
                    work
                    Thread.sleep(delay)
                }
                def work = {
                    val now = System.currentTimeMillis()
                    ctx.collect(self.fn(now))
                }
            })

            thread.setDaemon(true)
            thread.run()
        }

        override def cancel() : Unit = continue.set(false)
    }

    object FakeSource {
        def apply[T](delay: Long)(fn: Long => T) = new FakeSource[T](fn, delay)
    }

    class SimpleMap extends RichMapFunction[Int, Int] {

        def meter(group: MetricGroup, name: String) = {
            group.meter(
                name
                ,
                new DropwizardMeterWrapper(new com.codahale.metrics.Meter())
            )
        }

        var oneDigit: Meter = null

        var all: Meter = null

        var odd: Meter = null

        override def open(config : Configuration) : Unit = {

            val group =
                getRuntimeContext.getMetricGroup
                    .addGroup("logicalMetric")

            all = meter(group, "all")

            odd = meter(group, "odd")

            oneDigit = meter(group, "oneDigit")

        }

        override def map(value : Int) : Int = {
            all.markEvent()
            if (value < 10) oneDigit.markEvent()
            if (value % 2 == 1) odd.markEvent()
            value
        }
    }

    def job(env: StreamExecutionEnvironment, name: String) = {
        val in = env.addSource(FakeSource(100) { now => util.Random.nextInt(100) })

        in.map(new SimpleMap).addSink(new SinkFunction[Int] {
            override def invoke(value : Int) : Unit = println(value)
        })

        env.execute(name)
    }


    def submit(args: Array[String]): Unit = {

        if (args.length != 1) {
            System.err.println("USAGE: \n <job_name>")
            return
        }

        val name = args(0)

        val env = StreamExecutionEnvironment.getExecutionEnvironment

        job(env, name)
    }

    def remote(args: Array[String]): Unit = {

        if (args.length != 3) {
            System.err.println("USAGE: \n <job_name> <host> <port>")
            return
        }

        val name = args(0)
        val host = args(1)
        val port = args(2).toInt

        val jar = {
            "/mnt/mine/work/elasticreporter/module/examplejob/target/scala-2.11/examplejob-assembly-0.1.0-SNAPSHOT.jar"
            "target/scala-2.11/examplejob-assembly-0.1.0-SNAPSHOT.jar"
        }

        println(name, host, port)

        val env = StreamExecutionEnvironment.createRemoteEnvironment(host, port, jar)

        job(env, name)
    }

    def main(args: Array[String]): Unit  = submit(args)

}
