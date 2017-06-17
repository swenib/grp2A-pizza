package restSpec

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.Helpers.{GET, OK, contentType, route, status}
import play.api.test.FakeRequest
import play.test.WithApplication

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class EmployeesSpecs extends Specification {

  "EmployeesSpecs" should {

    "respond with a json representation for all employees" in new WithApplication{
      val response = route(FakeRequest(GET, "/api/employees")).get
      status(response) must equalTo(OK)
      contentType(response) must beSome.which(_ == "application/json")
    }

    "respond with a json representation for a specific employee" in new WithApplication {
      val response = route(FakeRequest(GET, "/api/employee/1")).get
      status(response) must equalTo(OK)
      contentType(response) must beSome.which(_ == "application/json")
    }

    /*
    "respond with a json representation with adding an employee" in new WithApplication {
      val response = route(FakeRequest(POST, "/api/employee")).get
      status(response) must equalTo(OK)
      contentType(response) must beSome.which(_ == "application/json")
    }
    */
  }
}
