package me.samei.xtool.esreporter.v1.common.v1.common

sealed trait Formattable[T] {
    def value: T
    def format: String
}

object Formattable {

    def instance[T](origin: T)(fn: T => String) : Formattable[T] = new Formattable[T] {

        override def value : T = origin

        override def format : String = OriginValue
    }

}


sealed trait FormattableValue { self =>
    def format : String = self match {
        case FormattableValue.Simple(value) => value
        case FormattableValue.Quotable(value) => s"\"${value}\""
        case FormattableValue.Multiple(value) => s"[${value.map{_.format}.mkString(",")}]"
    }
}

object FormattableValue {
    case class Simple(value: String) extends FormattableValue
    case class Quotable(value: String) extends FormattableValue
    case class Multiple[T <: FormattableValue](value: Seq[T]) extends FormattableValue
}




case class FormattedKeyValue(
    key : String,
    value : String,
    tags : Seq[String]
) {

    def append(tag : String) : FormattedKeyValue = copy(tags = tags :+ tag)

    def remove(tag: String) : FormattedKeyValue = copy(tags = tags.filter { i => i == tag } )

}

trait Formatter {

    def formatKey(key: String): String
    def

}