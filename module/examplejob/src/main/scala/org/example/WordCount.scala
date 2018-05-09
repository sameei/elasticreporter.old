package org.example

/**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

import org.apache.flink.api.common.functions.{MapFunction, RichMapFunction}
import org.apache.flink.api.common.io.OutputFormat
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.metrics.Counter
import org.apache.flink.streaming.api.operators.Output

/**
  * Implements the "WordCount" program that computes a simple word occurrence histogram
  * over some sample data
  *
  * This example shows how to:
  *
  *   - write a simple Flink program.
  *   - use Tuple data types.
  *   - write and use user-defined functions.
  */
object WordCount {
    def main(args : Array[String]) {

        if ( args.length != 1 ) throw new IllegalArgumentException("Application need a JobName")
        val jobName = args(0).trim;
        if ( jobName.isEmpty ) throw new IllegalArgumentException(s"Invalid JobName: ${jobName}")

        // set up the execution environment
        val env = ExecutionEnvironment.getExecutionEnvironment


        // get input data
        val text = env.fromElements("To be, or not to be,--that is the question:--",
            "Whether 'tis nobler in the mind to suffer", "The slings and arrows of outrageous fortune",
            "Or to take arms against a sea of troubles,")

        val counts = text.flatMap { _.toLowerCase.split("\\W+") }
            .map(new RichMapFunction[String, String] {

                var counter: Counter = null

                override def open(parameters : Configuration) : Unit = {

                    counter = this.getRuntimeContext
                        .getMetricGroup()
                        .addGroup("usergroup")
                        .counter("count")

                    super.open(parameters)
                }

                override def map(value : String) : String = {
                    counter.inc()
                    value
                }
            }).name("proxy-counter")
            .map { (_, 1) }.name("count-from-one")
            .groupBy(0)
            .sum(1).name("groupby-and-sum")

        // execute and print result
        // counts.print()
        // counts.collect()

        counts.output(new OutputFormat[(String, Int)] {

            override def configure(parameters : Configuration) : Unit = {}

            override def open(taskNumber : Int, numTasks : Int) : Unit = {}

            override def writeRecord(record : (String, Int)) : Unit = {}

            override def close() : Unit = {}

        }).name("null")


        env.execute(jobName)

    }
}
