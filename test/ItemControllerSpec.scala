import models.Item
import org.scalatestplus.play._
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import scala.concurrent.Future
import play.api.test.Helpers._


class ItemControllerSpec extends PlaySpec with MockData {

  "Item controller response" must {
    "return 200 when getting a valid item by id" in {

      val fake = FakeRequest("GET","/item/"+validData1String)
      val resp = itemController.getItem(validData1String).apply(fake)
      status(resp) mustBe 200

    }
    "return 404 when item doesnt exist" in {
      val fake = FakeRequest("GET","/item/"+noData1String)
      val resp = itemController.getItem(noData1String).apply(fake)
      status(resp) mustBe 404
    }

    "return 400 when invalid id" in {
      val uuidString = "test"
       val fake = FakeRequest("GET","/item/"+uuidString)
      val resp = itemController.getItem(uuidString).apply(fake)
      status(resp) mustBe 400
    }

    "return 200 when there are items that exist for a category" in {
      val fake = FakeRequest("GET","/item/category/fruit")
      when(mockMongo.findByProperties(catFruitProp)).thenReturn(Future.successful(sameCategory))
      val resp = itemController.showAllForCategory("fruit").apply(fake)
      status(resp) mustBe 200

    }

    "return 404 when there are no items that exist for a category" in {
      val fake = FakeRequest("GET","/item/category/fruit")
      when(mockMongo.findByProperties(catFruitProp)).thenReturn(Future.successful(List()))
      val resp = itemController.showAllForCategory("fruit").apply(fake)
      status(resp) mustBe 404
    }

    "return 200 when there are low stock items" in {
      val fake = FakeRequest("GET","/item/low")
      when(mockMongo.findByProperties(lowProp)).thenReturn(Future.successful(lowStock))
      val resp = itemController.showAllLowStock().apply(fake)
      status(resp) mustBe 200

    }

    "return 404 when there are no low stock items" in {
      val fake = FakeRequest("GET","/item/low")
      when(mockMongo.findByProperties(lowProp)).thenReturn(Future.successful(List()))
      val resp = itemController.showAllLowStock().apply(fake)
      status(resp) mustBe 404

    }
  }

  "Item controller data" must {
    "be correct when getting one item from an ID" in {

      val fake = FakeRequest("GET","/item/"+validData1String)
      val resp = itemController.getItem(validData1String).apply(fake)
      val itemMarshal = Json.fromJson[Item](contentAsJson(resp)) map {
        case i:Item => i mustBe validItem1
        case _ => fail("Invalid item")
      }
    }

    "be correct when returning low stock" in {

      val fake = FakeRequest("GET","/item/low")
      when(mockMongo.findByProperties(lowProp)).thenReturn(Future.successful(lowStock))
      val resp = itemController.showAllLowStock().apply(fake)

      val itemMarshal = Json.fromJson[List[Item]](contentAsJson(resp)) map {
        case i:List[Item] => i mustBe lowStock
        case _ => fail("Invalid item")
      }
    }

    "be correct when returning category data" in {

      val fake = FakeRequest("GET","/item/category/fruit")
      when(mockMongo.findByProperties(lowProp)).thenReturn(Future.successful(sameCategory))
      val resp = itemController.getItem(validData1String).apply(fake)

      val itemMarshal = Json.fromJson[List[Item]](contentAsJson(resp)) map {
        case i:List[Item] => i mustBe lowStock
        case _ => fail("Invalid item")
      }
    }


  }


}
