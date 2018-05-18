package com.sameei.xtool.elasticreporter.v1.common

object FormatterV1 {

    case class Val(key: String, value: String, tipe: Value.Type) extends Value

    case class FormatException(
        desc: String,
        rawKey: String,
        rawValue: String,
        cause: Option[Throwable]
    ) extends IllegalArgumentException(desc, cause.orNull) with data.BaseException

}

class FormatterV1 extends Formatter[FormatterV1.Val] {

    import FormatterV1._

    private val keyValidationRegex = "[\\|/\"\'\n\r]".r

    private def isInvalidKey(key: String) =
        keyValidationRegex.findFirstIn(key).isDefined

    private def checkInvalidKey(key: String, value: String) =
        if (isInvalidKey(key)) throw new FormatException(s"Invalid Key to Format: '${key}'", key, value, None)

    private val valueValidationRegex = "[\"\n\r]".r

    private def inInvalidValue(value: String) =
        valueValidationRegex.findFirstIn(value).isDefined

    private def checkInvalidStringValue(key: String, value: String) =
        if (inInvalidValue(value)) throw new FormatException(s"Invlaid Value to Format: '${value}'", key, value, None)

    override def formatString(rawKey : String, rawVal : String): Val  = {
        checkInvalidKey(rawKey, rawVal)
        checkInvalidStringValue(rawKey, rawVal)
        Val(rawKey, rawVal, Value.Qouted)
    }

    override def formatBool(rawKey : String, rawVal : Boolean) : Val = {
        val value = rawVal match {
            case true => "true"
            case false => "false"
        }
        checkInvalidKey(rawKey, value)
        Val(rawKey, value, Value.Simple)
    }

    override def formatInt(rawKey : String, rawVal : Int) : Val = {
        val value = rawVal.toString
        checkInvalidKey(rawKey, value)
        Val(rawKey, value, Value.Simple)
    }

    override def formatLong(rawKey : String, rawVal : Long) : Val = {
        val value = rawVal.toString
        checkInvalidKey(rawKey, value)
        Val(rawKey, value, Value.Simple)
    }

    override def formatFloat(rawKey : String, rawVal : Float) : Val = {
        val value = rawVal.toString
        checkInvalidKey(rawKey, value)
        Val(rawKey, value, Value.Simple)
    }

    override def formatDouble(rawKey : String, rawVal : Double) : Val = {
        val value = rawVal.toString
        checkInvalidKey(rawKey, value)
        Val(rawKey, value, Value.Simple)
    }

    protected def append(value: Val)(implicit buf: StringBuilder) = {
        buf.append('"').append(value.key).append("\": ")
        value.tipe match {
            case Value.Simple =>
                buf.append(value.value)
            case Value.Qouted =>
                buf.append('"').append(value.value).append('"')
        }
    }

    override def format(values : Seq[Val]) : String = {

        implicit val buf = new StringBuilder

        buf.append("{")

        val (head,tail) = values match {
            case Nil => // nothing
            case head :: Nil => append(head)
            case head :: tail=>
                append(head)
                tail.foreach { i => buf.append(',').append(i)}
        }

        buf.append("}").result()
    }
}
