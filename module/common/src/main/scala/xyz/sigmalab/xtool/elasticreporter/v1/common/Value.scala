package xyz.sigmalab.xtool.elasticreporter.v1.common

trait Value {
    def key: String
    def value: String
    def tipe: Value.Type
}

object Value {

    sealed trait Type
    case object Simple extends Type
    case object Qouted extends Type

}
