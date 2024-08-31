package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json.Json

@Singleton
class HomeController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  def index() =
    Action { implicit request: Request[AnyContent] =>
      val jsonResponse = Json.obj(
        "message" -> "Hello, this is a JSON response",
        "status"  -> "success"
      )
      Ok(jsonResponse)
    }
}
