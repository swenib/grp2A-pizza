package controllersSpec

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.Helpers.{BAD_REQUEST, OK, POST, SEE_OTHER, contentAsString, redirectLocation, running, status}
import play.api.test.{FakeApplication, FakeRequest}
import controllers.OrderController
import dbaccess.OrderDao
import models.Order
import scala.concurrent.duration.DurationLong
import akka.util.Timeout
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
  * @author Felix Thomas
  */
@RunWith(classOf[JUnitRunner])
class OrderControllerSpec extends Specification{
  implicit val duration: Timeout = 20 seconds

  def memDB[T](code: => T) =
    running(FakeApplication(additionalConfiguration = Map(
      "db.default.driver" -> "org.h2.Driver",
      "db.default.url" -> "jdbc:h2:mem:test;MODE=PostgreSQL"
    )))(code)

  val time: Int = (2 * (19000 / 1000)) + (10 * 1)
  val deliveryTime = DateTimeFormat.forPattern("kk:mm+-+DD.MM.YYYY").print(DateTime.now())

  "OrderController" should{

    "create an order" in memDB {
      val request = FakeRequest(POST, "/createOrder").withFormUrlEncodedBody(
        "userID" -> "1",
        "pizzaID" -> "1",
        "productID" -> "0",
        "pizzaAmount" -> "1",
        "pizzaSize" -> "14",
        "productAmount" -> "0",
        "extraOneID" -> "0",
        "extraTwoID" -> "0",
        "extraThreeID" -> "0"
      )
      val result = OrderController.createOrder()(request)
      status(result) must equalTo(SEE_OTHER)
      // redirectLocation(result) must beSome("/newOrderCreated?id=1&customerID=1.0&pizzaID=1.0&productID=0.0&pizzaName=Margherita&productName=&pizzaAmount=1.0&pizzaSize=14.0&pizzaPrice=0.6&productAmount=0.0&productPrice=0.0&extrasName=%28%29&extraTotalPrice=0.0&totalPrice=8.4&orderTime=" + DateTimeFormat.forPattern("kk:mm+-+DD.MM.YYYY").print(DateTime.now()).replaceAll(":","%3A") + "&status=Bestellung+empfangen&deliveryTime=" + deliveryTime.replaceAll(":","%3A"))
    }

    "create an order bad request" in memDB {
      val request = FakeRequest(POST, "/createOrder").withFormUrlEncodedBody(
        "pizzaID" -> "1",
        "productID" -> "0",
        "pizzaAmount" -> "1",
        "pizzaSize" -> "14",
        "productAmount" -> "0",
        "extraOneID" -> "0",
        "extraTwoID" -> "0",
        "extraThreeID" -> "0"
      )
      val result = OrderController.createOrder()(request)
      status(result) must equalTo(BAD_REQUEST)
      redirectLocation(result) must beNone
    }

    "set the status of an order" in memDB {
      val add = OrderDao.createOrder(Order(-1,1,1,0,"N/A", "N/A",1,14,-1,0,-1,0,"N/A", 0,0,"N/A", 0,0,"N/A",0,0,
        DateTimeFormat.forPattern("kk:mm+-+DD.MM.YYYY").print(DateTime.now()),"N/A", deliveryTime))
      val request = FakeRequest(POST, "/setOrderStatus").withFormUrlEncodedBody(
        "orderID" -> "1",
        "orderStatusKZ" -> "Bestellung erhalten"
      )
      val result = OrderController.setStatusOrder()(request)
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome("/allOrderDetails")
    }

    "set the status of an order bad request" in memDB {
      val add = OrderDao.createOrder(Order(-1,1,1,0,"N/A", "N/A",1,14,-1,0,-1,0,"N/A", 0,0,"N/A", 0,0,"N/A",0,0,
        DateTimeFormat.forPattern("kk:mm - DD.MM.YYYY").print(DateTime.now()),"N/A", deliveryTime))
      val request = FakeRequest(POST, "/setOrderStatus").withFormUrlEncodedBody(
        "orderStatusKZ" -> "Bestellung erhalten"
      )
      val result = OrderController.setStatusOrder()(request)
      status(result) must equalTo(BAD_REQUEST)
      redirectLocation(result) must beNone
    }

    "show a selected order" in memDB {
      val add = OrderDao.createOrder(Order(-1,1,1,0,"N/A", "N/A",1,14,-1,0,-1,0,"N/A", 0,0,"N/A", 0,0,"N/A",0,0,
        DateTimeFormat.forPattern("kk:mm - DD.MM.YYYY").print(DateTime.now()),"Bestellung empfangen", deliveryTime))
      val request = FakeRequest(POST, "/showSelectedOrder").withFormUrlEncodedBody(
        "orderID" -> "1"
      )
      val result = OrderController.showSelectedOrder()(request)
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome("/showOrder?customerID=1.0&pizzaID=1.0&productID=0.0&pizzaName=Margherita&productName=&pizzaAmount=1.0&pizzaSize=14.0&pizzaPrice=0.6&productAmount=0.0&productPrice=0.0&totalPrice=8.4&orderTime=" + DateTimeFormat.forPattern("kk:mm+-+DD.MM.YYYY").print(DateTime.now()).replaceAll(":","%3A") + "&status=Bestellung+empfangen&extrasName=empty&extraTotalPrice=0.0")
    }

    "show a selected order bad request" in memDB {
      val add = OrderDao.createOrder(Order(-1, 1, 1, 0, "N/A", "N/A", 1, 14, -1, 0, -1, 0, "N/A", 0, 0, "N/A", 0, 0, "N/A", 0, 0,
        DateTimeFormat.forPattern("kk:mm - DD.MM.YYYY").print(DateTime.now()), "Bestellung empfangen", deliveryTime))
      val request = FakeRequest(POST, "/showSelectedOrder").withFormUrlEncodedBody(
        "orderiD" -> "1"
      )
      val result = OrderController.showSelectedOrder()(request)
      status(result) must equalTo(BAD_REQUEST)
      redirectLocation(result) must beNone
    }

    "delete an order" in memDB {
      val add = OrderDao.createOrder(Order(-1, 1, 1, 0, "N/A", "N/A", 1, 14, -1, 0, -1, 0, "N/A", 0, 0, "N/A", 0, 0, "N/A", 0, 0,
        DateTimeFormat.forPattern("kk:mm - DD.MM.YYYY").print(DateTime.now()), "Bestellung empfangen", deliveryTime))
      val request = FakeRequest(POST, "/deleteOrder").withFormUrlEncodedBody(
        "orderID" -> "1"
      )
      val result = OrderController.deleteOrder()(request)
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome("/orderDeleted")
    }

    "delete an order bad request" in memDB {
      val add = OrderDao.createOrder(Order(-1, 1, 1, 0, "N/A", "N/A", 1, 14, -1, 0, -1, 0, "N/A", 0, 0, "N/A", 0, 0, "N/A", 0, 0,
        DateTimeFormat.forPattern("kk:mm - DD.MM.YYYY").print(DateTime.now()), "Bestellung empfangen", deliveryTime))
      val request = FakeRequest(POST, "/deleteOrder").withFormUrlEncodedBody(
        "orderiD" -> "1"
      )
      val result = OrderController.deleteOrder()(request)
      status(result) must equalTo(BAD_REQUEST)
      redirectLocation(result) must beNone
    }

    "deactivate an order" in memDB {
      val add = OrderDao.createOrder(Order(-1, 1, 1, 0, "N/A", "N/A", 1, 14, -1, 0, -1, 0, "N/A", 0, 0, "N/A", 0, 0, "N/A", 0, 0,
        DateTimeFormat.forPattern("kk:mm - DD.MM.YYYY").print(DateTime.now()), "Bestellung empfangen", deliveryTime))
      val request = FakeRequest(POST, "/deactivateOrder").withFormUrlEncodedBody(
        "orderID" -> "1"
      )
      val result = OrderController.deactivateOrder()(request)
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome("/orderDeleted")
    }

    "deactivate an order bad request" in memDB {
      val add = OrderDao.createOrder(Order(-1, 1, 1, 0, "N/A", "N/A", 1, 14, -1, 0, -1, 0, "N/A", 0, 0, "N/A", 0, 0, "N/A", 0, 0,
        DateTimeFormat.forPattern("kk:mm - DD.MM.YYYY").print(DateTime.now()), "Bestellung empfangen", deliveryTime))
      val request = FakeRequest(POST, "/deactivateOrder").withFormUrlEncodedBody(
        "orderiD" -> "1"
      )
      val result = OrderController.deactivateOrder()(request)
      status(result) must equalTo(BAD_REQUEST)
      redirectLocation(result) must beNone
    }

    "show view for a new order" in memDB {
      val request = FakeRequest(POST, "/createOrder").withFormUrlEncodedBody(
        "userID" -> "1",
        "pizzaID" -> "1",
        "productID" -> "0",
        "pizzaAmount" -> "1",
        "pizzaSize" -> "14",
        "productAmount" -> "0",
        "extraOneID" -> "0",
        "extraTwoID" -> "0",
        "extraThreeID" -> "0"
      )
      val result = OrderController.newOrderCreated(1,1,1,0,"Margherita","N/A",1,14,0.6,0,0,"N/A",0,0,
        DateTimeFormat.forPattern("kk:mm - DD.MM.YYYY").print(DateTime.now()), "Bestellung empfangen", deliveryTime)(request)
      status(result) must equalTo(OK)
      contentAsString(result) must contain ("Margherita") contain "14" contain "Bestellung"
    }

    "show the showOrder view" in memDB {
      val request = FakeRequest(POST, "/createOrder").withFormUrlEncodedBody(
        "userID" -> "1",
        "pizzaID" -> "1",
        "productID" -> "0",
        "pizzaAmount" -> "1",
        "pizzaSize" -> "14",
        "productAmount" -> "0",
        "extraOneID" -> "0",
        "extraTwoID" -> "0",
        "extraThreeID" -> "0"
      )
      val result = OrderController.showOrder(1,1,0,"Margherita","N/A",1,14,0.6,0,0,0,
        DateTimeFormat.forPattern("kk:mm - DD.MM.YYYY").print(DateTime.now()), "Bestellung empfangen","N/A",0)(request)
      status(result) must equalTo(OK)
      contentAsString(result) must contain ("Margherita") contain "14" contain "Bestellung"
    }

    "show the view for deleted orders" in memDB {
      val request = FakeRequest(POST, "/createOrder").withFormUrlEncodedBody(
        "userID" -> "1",
        "pizzaID" -> "1",
        "productID" -> "0",
        "pizzaAmount" -> "1",
        "pizzaSize" -> "14",
        "productAmount" -> "0",
        "extraOneID" -> "0",
        "extraTwoID" -> "0",
        "extraThreeID" -> "0"
      )
      val result = OrderController.orderDeleted()(request)
      status(result) must equalTo(OK)
      contentAsString(result) must contain("storniert") contain "erfolgreich" contain "Bestellung"
    }
  }
}
