package com.sameei.xtool.elasticreporter.v1.flink.lego

import com.sameei.xtool.elasticreporter.v1.flink.lego.data.{ FlinkMetricRef, MetricRef }
import org.apache.flink.metrics.MetricConfig

import scala.util.{ Either, Left, Right }

trait NameBy { def nameOf(ref: MetricRef): String }

import util.WithLogger

object NameBy {

    def combine(parts: Seq[String], vars: Map[String, String]) = {
        parts.foldLeft(new StringBuilder) { (buf, p) =>
            if (!buf.isEmpty) buf.append(".")
            buf.append(vars get p getOrElse p)
            buf
        }.toString
    }

    case class Origin(override val name: String ) extends NameBy with WithLogger {

        override def nameOf(ref : MetricRef) : String = {
            val metricName = ref.id
            if (logger.isTraceEnabled()) logger.trace(s"Name: ${metricName}, ${ref.desc}")
            metricName
        }

        override def toString : String = getClass.getName
    }

    case class ScopeDropLeft(override val name: String, limit: Int) extends NameBy with WithLogger {
        override def nameOf(ref : MetricRef) : String = {
            val all = ref.scope
            val finalResult =
                if (all.size < limit) ref.name // warning
                else if (all.size == limit) return ref.name
                else s"${all.drop(limit).mkString(".")}.${ref.name}"

            if (logger.isTraceEnabled()) logger.trace(s"Name: ${finalResult}, Limit: ${limit}, All: ${limit}, ${ref.desc}")

            finalResult
        }
    }

    case class SelectScope(override val name: String, scopeName: String) extends NameBy with WithLogger {

        def partsOf(ref: MetricRef) = ref.id.split('.').toList

        override def nameOf(ref: MetricRef): String = {

            val allParts = partsOf(ref)
            val partsOfName = allParts.dropWhile { i => i != scopeName }

            val finalResult = partsOfName match {
                case Nil | _ :: Nil => ""
                case _ :: tail => tail.mkString(".")
            }

            if (logger.isTraceEnabled()) logger.trace(s"Name: ${finalResult}, PartsOfName: ${allParts}, ${ref.desc}")

            finalResult
        }
    }

    def apply(name: String, config: MetricConfig): NameBy = {
        val maybe = for {
            _ <- fromSelectScope(s"${name}.selectscope", config).right
            _ <- fromScopeDropLeft(s"${name}.scopedropleft", config).right
        } yield ()

        maybe match {
            case Left(inst) => inst
            case Right(_) => new Origin(s"${name}.originid")
        }
    }

    private def fromScopeDropLeft(name: String, config: MetricConfig): Either[NameBy, Unit] = {
        val limit = config.getInteger("name-by.scope-drop-left", -1)
        if (limit > 0) Left(new ScopeDropLeft(name, limit))
        else Right(())
    }

    private def fromSelectScope(name: String, config: MetricConfig): Either[NameBy, Unit] = {
        Option(config.getString("name-by.select-scope", null)) match {
            case None => Right(())
            case Some(scope) => Left(new SelectScope(name, scope))
        }
    }

}
