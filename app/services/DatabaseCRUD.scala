package services


import java.util.UUID
import models.Item
import reactivemongo.api._
import play.api.libs.json._
import reactivemongo.api.commands.WriteConcernError
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.json._

/**
  * Created by pritesh on 20/02/16.
  */
trait DatabaseCRUD[T] {


  def findByID(id:UUID) :Future[Option[T]]
  def findByProperties(properties: JsObject) :Future[List[T]]
  def create(item:T) : Future[Either[String,T]]
  def update(item:T) :Future[Either[String,T]]
  def delete(id:UUID) :Future[Either[String,UUID]]

}
abstract class MongoItemCRUD extends DatabaseCRUD[Item] {



  def collection : JSONCollection


  //reads
  override def findByID(id:UUID): Future[Option[Item]] = {
    collection.find(Json.obj("_id" -> id)).one[Item]
  }

  override def findByProperties(properties: JsObject) = {
    collection.find(properties).cursor[Item](ReadPreference.Primary).
      collect[List]()
  }

  //writes
  override def create(item:Item) : Future[Either[String,Item]] = {
    findByID(item._id) flatMap {
      case Some(v)  =>
        val newItem = item.copy(_id = UUID.randomUUID())
        collection.insert(newItem) map {
          res => if (res.ok) Right(newItem)
          else Left(res.message)
        }
      case None =>
        collection.insert(item) map {
          res => if (res.ok) Right(item)
          else Left(res.message)
        }
    }
  }

  override def update(item:Item) : Future[Either[String,Item]] = {
    collection.update(Json.obj("_id" -> item._id), item) map {
      res => if (res.ok) Right(item)
      else Left(res.message)
    }
  }

  override def delete(id:UUID) : Future[Either[String,UUID]] = {
    findByID(id) flatMap{
      case Some(x) => collection.remove(Json.obj("_id" -> id)) map {
        res => if (res.ok) Right(id)
        else Left(res.message)

      }
      case None => Future.successful(Left("Item does not exist"))
    }

  }


}

class MongoItemService(db:DB) extends MongoItemCRUD {
  override def collection = db.collection("items")
}
