package actions

import org.scalatestplus.play._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.FakeRequest
import services.AuthService
import org.mockito.Mockito._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthenticatedActionSpec extends PlaySpec with MockitoSugar {

  "AuthenticatedAction" should {

    "allow access when a valid token is provided" in {
      val mockAuthService = mock[AuthService]
      val bodyParsers     = new BodyParsers.Default(stubControllerComponents().parsers)
      val action          = new AuthenticatedAction(bodyParsers, mockAuthService)

      when(mockAuthService.validateToken("validToken"))
        .thenReturn(Future.successful(Some("userId")))

      val request = FakeRequest().withHeaders("Authorization" -> "Bearer validToken")
      val result  = action.invokeBlock(
        request,
        { req: AuthenticatedRequest[_] =>
          Future.successful(Results.Ok(s"User ID: ${req.userId}"))
        }
      )

      status(result) mustBe OK
      contentAsString(result) mustBe "User ID: userId"
    }

    "return Unauthorized when an invalid token is provided" in {
      val mockAuthService = mock[AuthService]
      val bodyParsers     = new BodyParsers.Default(stubControllerComponents().parsers)
      val action          = new AuthenticatedAction(bodyParsers, mockAuthService)

      when(mockAuthService.validateToken("invalidToken"))
        .thenReturn(Future.successful(None))

      val request = FakeRequest().withHeaders("Authorization" -> "Bearer invalidToken")
      val result  = action.invokeBlock(
        request,
        { _: AuthenticatedRequest[_] =>
          Future.successful(Results.Ok("This should not be reached"))
        }
      )

      status(result) mustBe UNAUTHORIZED
    }

    "return Unauthorized when no token is provided" in {
      val mockAuthService = mock[AuthService]
      val bodyParsers     = new BodyParsers.Default(stubControllerComponents().parsers)
      val action          = new AuthenticatedAction(bodyParsers, mockAuthService)

      val request = FakeRequest()
      val result  = action.invokeBlock(
        request,
        { _: AuthenticatedRequest[_] =>
          Future.successful(Results.Ok("This should not be reached"))
        }
      )

      status(result) mustBe UNAUTHORIZED
    }
  }
}
