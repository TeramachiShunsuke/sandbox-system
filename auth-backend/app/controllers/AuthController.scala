package controllers

import play.api.mvc._
import services.{AuthService, UserService}
import play.api.libs.json.{Json}
import scala.concurrent.Future
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AuthController @Inject() (
  cc: ControllerComponents,
  authService: AuthService,
  userService: UserService
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def token: Action[AnyContent] =
    Action.async { implicit request =>
      val formData = request.body.asFormUrlEncoded.getOrElse(Map.empty)
      val username = formData.get("username").flatMap(_.headOption).getOrElse("")
      val password = formData.get("password").flatMap(_.headOption).getOrElse("")

      // AuthServiceでユーザー認証を行い、トークンを生成
      userService.authenticateUser(username, password).flatMap {
        case Some(user) =>
          authService.createTokens(user.id, 3600, 7200).map { token =>
            Ok(
              Json.obj(
                "access_token"  -> token.accessToken,
                "id_token"      -> token.idToken,
                "refresh_token" -> token.refreshToken,
                "token_type"    -> "bearer",
                "expires_in"    -> 3600
              )
            )
          }
        case None       =>
          Future.successful(Unauthorized(Json.obj("error" -> "Invalid credentials")))
      }
    }

  def refreshToken: Action[AnyContent] =
    Action.async { implicit request =>
      val formData     = request.body.asFormUrlEncoded.getOrElse(Map.empty)
      val refreshToken = formData.get("refresh_token").flatMap(_.headOption).getOrElse("")

      authService.refreshToken(refreshToken).map {
        case Some(token) =>
          Ok(
            Json.obj(
              "access_token"  -> token.accessToken,
              "id_token"      -> token.idToken,
              "refresh_token" -> token.refreshToken,
              "token_type"    -> "bearer",
              "expires_in"    -> 3600
            )
          )
        case None        =>
          Unauthorized(Json.obj("error" -> "Invalid or expired refresh token"))
      }
    }
}
