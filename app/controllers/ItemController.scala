package controllers


import java.util.UUID

import models.Forms.{StockUpdateForm, ItemForm}
import models.Item
import play.api.data._
import play.api.data.Forms._

import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents, MongoController}
import services.{MongoItemService, DatabaseCRUD}
import javax.inject.Inject
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.api.libs.json._


/**
  * Created by pritesh on 20/02/16.
  */
class ItemController @Inject()(val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents {

  def dbService:DatabaseCRUD[Item] = new MongoItemService(db)

  val itemForm = Form(
    mapping(
      "name"      -> nonEmptyText,
      "category"  -> nonEmptyText,
      "stock"     -> number(min = 0, max = 1000)
    )(ItemForm.apply)(ItemForm.unapply)
  )

  val stockUpdateForm = Form(
    mapping(
      "stock"     -> number(min = 0, max = 1000)
    )(StockUpdateForm.apply)(StockUpdateForm.unapply)
  )



  def getItem(id:String) = Action.async {
    try{
      dbService.findByID(UUID.fromString(id)) map{
        case Some(x)  => Ok(Json.toJson(x))
        case None     => NotFound("Item does not exist")
      }

    } catch {
      case e:Exception => Future.successful(BadRequest(e.getMessage))
    }

  }

  def showAllForCategory(category:String) = Action.async {
    val prop = Json.obj("category" -> category)
    dbService.findByProperties(prop) map {
      value =>
        if (value.size > 0) Ok(Json.toJson(value))
        else NotFound("No items for that category")

    } recover {
      case x => BadRequest("Category search failed" + x.getMessage)

    }
  }

  def showAllLowStock() = Action.async  {
    val limit = Json.obj("$lt" -> 10 )
    val prop = Json.obj("stock" -> limit)
    dbService.findByProperties(prop) map {
      value =>
        if (value.size > 0) Ok(Json.toJson(value))
        else NotFound("No low stock items")

    } recover {
      case x => BadRequest("Low stock search failed")

    }
  }

  def createItem() = Action.async  {
    implicit request =>
    itemForm.bindFromRequest.fold(
      invalid => {
        Future.successful(BadRequest("Invalid data"))
      },
      valid => {
        val item = Item(UUID.randomUUID(),valid.name,valid.category,valid.stock)
        dbService.create(item) map {
          case Right(x) => Ok(s"Item: ${x._id.toString} added successfully")
          case Left(s)  => BadRequest(s)
        }
      }
    )

  }

  def updateItemStock(id:String) = Action.async  {
    implicit request =>
      stockUpdateForm.bindFromRequest.fold(
        invalid => {
          Future.successful(BadRequest("Invalid data"))
        },
        valid => {
          try{
            dbService.findByID(UUID.fromString(id)) flatMap{
              case Some(x) =>
                dbService.update(x.copy(stock=valid.stock)) map {
                  case Right(x) => Ok(s"Item: ${x._id.toString} updated successfully")
                  case Left(s)  => BadRequest(s)
              }
              case None => Future.successful(NotFound("Item does not exist"))
            }

          } catch {
            case e:Exception => Future.successful(BadRequest(e.getMessage))
          }
        }
      )

  }

  def deleteItem(id:String) = Action.async {
    try{
      dbService.delete(UUID.fromString(id)) map {
        case Right(x) => Ok(s"Item ${x.toString()} successfully deleted")
        case Left(s)  => BadRequest(s)
      }
    } catch {
      case e:Exception => Future.successful(BadRequest(e.getMessage))
    }
  }

}
