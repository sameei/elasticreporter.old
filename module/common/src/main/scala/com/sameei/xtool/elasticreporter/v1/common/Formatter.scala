package com.sameei.xtool.elasticreporter.v1.common

trait Formatter {

    type Val <: Value

    def formatString(rawKey: String, rawVal: String) : Val

    def formatBool(rawKey: String, rawVal: Boolean): Val

    def formatInt(rawKey: String, rawVal: Int) : Val

    def formatLong(rawKey: String, rawVal: Long): Val

    def formatFloat(rawKey: String, rawVal: Float): Val

    def formatDouble(rawKey: String, rawVal: Double): Val

    def format(values: Seq[Val]): String

}

