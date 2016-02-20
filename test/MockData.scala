import java.util.UUID

import controllers.ItemController
import models.Item
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import services.MongoItemService

import scala.concurrent.Future

/**
  * Created by pritesh on 20/02/16.
  */
trait MockData extends MockitoSugar{

  val mockMongo = mock[MongoItemService]
  val mockMongoAPI = mock[ReactiveMongoApi]
  val itemController = new ItemController(mockMongoAPI){override def dbService = mockMongo}
  val limit = Json.obj("$lt" -> 10 )
  val lowProp = Json.obj("stock" -> limit)
  val catFruitProp = Json.obj("category" -> "fruit")


  //set up mock data
  /**Valid items**/
  val validData1String = "2ce89b22-ae0b-45e6-93cc-94ff5c4f0d8e"
  val validItem1UUID = UUID.fromString(validData1String)
  val validItem1 = Item(validItem1UUID,"apple","fruit",3)
  when(mockMongo.findByID(validItem1UUID)).thenReturn(Future.successful(Option(validItem1)))

  val validData2String = "383bc36d-c85f-4031-9714-1b4244a20a9f"
  val validItem2UUID = UUID.fromString(validData2String)
  val validItem2 = Item(validItem2UUID,"toast","bread",7)
  when(mockMongo.findByID(validItem2UUID)).thenReturn(Future.successful(Option(validItem2)))

  val validData3String = "89de3bfc-16b5-4496-8e3b-9706352d7499"
  val validItem3UUID = UUID.fromString(validData3String)
  val validItem3 = Item(validItem3UUID,"orange","fruit",12)
  when(mockMongo.findByID(validItem3UUID)).thenReturn(Future.successful(Option(validItem3)))

  val lowStock = List(validItem1,validItem2)
  val sameCategory = List(validItem1,validItem2)

  /**Non existing but valid id**/
  val noData1String = "938d488d-098e-4a35-9096-6e3f29a04622"
  val noItem1UUID = UUID.fromString(noData1String)
  when(mockMongo.findByID(noItem1UUID)).thenReturn(Future.successful(None))

}
