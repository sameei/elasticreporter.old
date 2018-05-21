package com.sameei.xtool.elasticreporter.v1.flink.lego

import com.sameei.xtool.elasticreporter.v1.flink.lego.data.MetricRef
import org.apache.flink.metrics.MetricConfig

trait NameBy { def nemeOf(ref: MetricRef): String }

object NameBy {

    class Origin extends NameBy {
        override def nemeOf(ref : MetricRef) : String = ref.group.getMetricIdentifier(ref.name)

        override def toString : String = getClass.getName
    }

    case class ScopeDropLeft(limit: Int) extends NameBy {
        override def nemeOf(ref : MetricRef) : String = {
            val all = ref.group.getScopeComponents
            if (all.size < limit) ref.name // warning
            else if (all.size == limit) return ref.name
            else s"${all.drop(limit).mkString(".")}.${ref.name}"
        }
    }

    def apply(config: MetricConfig): NameBy = {

        val limit = config.getInteger("name-by.scope-drop-left", -1)
        if ( limit > 0 ) new ScopeDropLeft(limit)
        else new Origin
    }

}
