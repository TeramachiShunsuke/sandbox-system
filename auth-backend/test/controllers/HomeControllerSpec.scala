package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._

/** Add your spec here. You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see
  * https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "HomeController GET" should {

    "return a JSON response with a success status" in {
      // Arrange
      val controller = inject[HomeController]
      val request    = FakeRequest(GET, "/")

      // Act
      val result = controller.index().apply(request)

      // Assert
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      val json = contentAsJson(result)
      (json \ "message").as[String] mustBe "Hello, this is a JSON response"
      (json \ "status").as[String] mustBe "success"
    }
  }
}
