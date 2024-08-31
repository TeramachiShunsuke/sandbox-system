package controllers

import play.api.mvc._
import javax.inject._
import scala.concurrent.ExecutionContext
import play.api.libs.json.{Json, JsValue}
import models.{User, UserForm}
import scala.concurrent.Future
import services.UserService
import actions.AuthenticatedAction
import play.api.Logger

@Singleton
class UserController @Inject() (
  cc: ControllerComponents,
  userService: UserService,
  authenticatedAction: AuthenticatedAction
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  private val logger                = Logger(this.getClass)
  // ユーザー一覧の取得
  def listUsers: Action[AnyContent] = authenticatedAction.async { request =>
    userService.listAllUsers.map { users =>
      Ok(Json.toJson(users))
    }
  }

  // 特定のユーザーの取得
  def getUser(id: String): Action[AnyContent] = authenticatedAction.async { request =>
    userService.getUserById(id).map {
      case Some(user) => Ok(Json.toJson(user))
      case None       => NotFound(Json.obj("error" -> "User not found"))
    }
  }

  def createUser: Action[JsValue] = authenticatedAction.async(parse.json) { request =>
    request.body
      .validate[UserForm]
      .fold(
        errors => {
          logger.error("Validation failed with errors: " + errors)
          Future.successful(BadRequest(Json.obj("error" -> "Invalid input")))
        },
        formData => {
          (for {
            existingUserByUsername <- userService.getUserByUsername(formData.username)
            if existingUserByUsername.isEmpty
            existingUserByEmail    <- userService.getUserByEmail(formData.email)
            if existingUserByEmail.isEmpty
            user                   <- userService.createUser(formData.username, formData.password, formData.email)
          } yield {
            Created(Json.obj("id" -> user.id, "username" -> user.username, "email" -> user.email))
          }).recover {
            case _: NoSuchElementException   =>
              logger.error("Username or Email already exists")
              BadRequest(Json.obj("error" -> "Username or Email already exists"))
            case _: IllegalArgumentException =>
              logger.error("Invalid input data")
              BadRequest(Json.obj("error" -> "Invalid input"))
            case ex: Throwable               =>
              logger.error("Unexpected error: " + ex.getMessage)
              InternalServerError(
                Json.obj("error" -> "Failed to create user", "details" -> ex.getMessage)
              )
          }
        }
      )
  }

  // ユーザーの更新
  def updateUser(id: String): Action[JsValue] = authenticatedAction.async(parse.json) { request =>
    request.body
      .validate[User]
      .fold(
        errors => Future.successful(BadRequest(Json.obj("error" -> "Invalid user data"))),
        userData => {
          userService
            .updateUser(id, userData)
            .map {
              case Some(user) => Ok(Json.toJson(user))
              case None       => NotFound(Json.obj("error" -> "User not found"))
            }
            .recover { case ex: Throwable =>
              InternalServerError(
                Json.obj("error" -> "Failed to update user", "details" -> ex.getMessage)
              )
            }
        }
      )
  }

  // ユーザーの論理削除
  def deleteUser(id: String): Action[AnyContent] = authenticatedAction.async { request =>
    userService.deleteUser(id).map {
      case Some(_) => NoContent
      case None    => NotFound(Json.obj("error" -> "User not found"))
    }
  }

  // APIKEYの更新
  def updateApikey(id: String): Action[JsValue] = authenticatedAction.async(parse.json) { request =>
    (request.body \ "apikey").asOpt[String] match {
      case Some(newApikey) =>
        userService.updateApikey(id, newApikey).map {
          case Some(apikey) => Ok(Json.obj("apikey" -> apikey))
          case None         => NotFound(Json.obj("error" -> "User not found"))
        }
      case None            =>
        Future.successful(BadRequest(Json.obj("error" -> "Missing apikey in request body")))
    }
  }
}
