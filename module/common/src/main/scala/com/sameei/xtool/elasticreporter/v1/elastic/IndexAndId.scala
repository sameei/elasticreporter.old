package com.sameei.xtool.elasticreporter.v1.elastic

import com.sameei.xtool.elasticreporter.v1.common.ReportContext

import scala.util.matching.Regex

case class IndexAndId(
    indexPattern: String,
    idPattern: String
) {

    protected def generate(
        pattern: String,
        vars: Map[String, String]
    )= {
        // https://stackoverflow.com/questions/9658701/scala-regex-replaceallin-cant-replace-when-replace-string-looks-like-a-regex
        vars.foldLeft(pattern) { case (pt, (k,v)) => pt.replaceAll(k, Regex.quoteReplacement(v)) }
    }

    def index(vars: Map[String, String]): String = generate(indexPattern, vars)

    def id(vars: Map[String, String]): String = generate(idPattern, vars)

}