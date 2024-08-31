package actions

import play.api.mvc._
import services.AuthService
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

// AuthenticatedRequest クラス
class AuthenticatedRequest[A](val userId: String, request: Request[A])
    extends WrappedRequest[A](request)

// AuthenticatedAction クラス
class AuthenticatedAction @Inject() (
  val parser: BodyParsers.Default,
  authService: AuthService
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[AuthenticatedRequest, AnyContent] {

  // invokeBlock メソッドの正しい実装
  override def invokeBlock[A](
    request: Request[A],
    block: AuthenticatedRequest[A] => Future[Result]
  ): Future[Result] = {
    // Authorization ヘッダーの検証
    request.headers.get("Authorization") match {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val token = authHeader.substring(7)
        // トークンの検証
        authService.validateToken(token).flatMap {
          case Some(userId) =>
            val authRequest = new AuthenticatedRequest(userId, request)
            block(authRequest)
          case None         =>
            Future.successful(Results.Unauthorized("Invalid token"))
        }
      case _                                                    =>
        Future.successful(Results.Unauthorized("Authorization header missing"))
    }
  }
}
