package models

import java.util.UUID

import play.api.libs.json.Json

/**
  * Created by pritesh on 20/02/16.
  */
case class Item(_id:UUID, name:String, category:String, stock:Int )
object Item {
  implicit val formats = Json.format[Item]
}
