package controllers

import org.scalatestplus.play._
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import play.api.test._
import play.api.test.Helpers._
import services.{AuthService, UserService}
import play.api.libs.json._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import models.{User, Token}
import java.sql.Timestamp
import java.time.Instant

class AuthControllerSpec extends PlaySpec with MockitoSugar {

  "AuthController token" should {

    "return tokens for valid credentials" in {
      // モックの設定
      val authService = mock[AuthService]
      val userService = mock[UserService]
      val controller  = new AuthController(stubControllerComponents(), authService, userService)

      // モックの振る舞いを設定
      val mockUser = Some(
        User(
          "1",
          "testuser",
          "password",
          "test@example.com",
          "apikey",
          isDeleted = false,
          Some(Timestamp.from(Instant.now())),
          Some(Timestamp.from(Instant.now()))
        )
      )
      when(userService.authenticateUser(any[String], any[String]))
        .thenReturn(Future.successful(mockUser))

      val mockToken = Token(
        "1",
        "1",
        "access-token",
        Some("id-token"),
        Some("refresh-token"),
        Timestamp.from(Instant.now()),
        Timestamp.from(Instant.now()),
        isRevoked = false
      )
      when(authService.createTokens(any[String], any[Int], any[Int]))
        .thenReturn(Future.successful(mockToken))

      // テストするリクエストを作成
      val request = FakeRequest(POST, "/token").withFormUrlEncodedBody(
        "username" -> "testuser",
        "password" -> "password"
      )

      // テストを実行
      val result = controller.token.apply(request)

      // アサーション
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      val json = contentAsJson(result)
      (json \ "access_token").as[String] mustBe "access-token"
      (json \ "id_token").as[String] mustBe "id-token"
      (json \ "refresh_token").as[String] mustBe "refresh-token"
      (json \ "token_type").as[String] mustBe "bearer"
      (json \ "expires_in").as[Int] mustBe 3600
    }

    "return Unauthorized for invalid credentials" in {
      // モックの設定
      val authService = mock[AuthService]
      val userService = mock[UserService]
      val controller  = new AuthController(stubControllerComponents(), authService, userService)

      // モックの振る舞いを設定
      when(userService.authenticateUser(any[String], any[String]))
        .thenReturn(Future.successful(None))

      // テストするリクエストを作成
      val request = FakeRequest(POST, "/token").withFormUrlEncodedBody(
        "username" -> "testuser",
        "password" -> "wrongpassword"
      )

      // テストを実行
      val result = controller.token.apply(request)

      // アサーション
      status(result) mustBe UNAUTHORIZED
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] mustBe "Invalid credentials"
    }
  }

  "AuthController refreshToken" should {

    "return new tokens for a valid refresh token" in {
      // モックの設定
      val authService = mock[AuthService]
      val userService = mock[UserService]
      val controller  = new AuthController(stubControllerComponents(), authService, userService)

      // モックの振る舞いを設定
      val mockToken = Some(
        Token(
          "1",
          "1",
          "new-access-token",
          Some("new-id-token"),
          Some("new-refresh-token"),
          Timestamp.from(Instant.now()),
          Timestamp.from(Instant.now()),
          isRevoked = false
        )
      )
      when(authService.refreshToken(any[String])).thenReturn(Future.successful(mockToken))

      // テストするリクエストを作成
      val request = FakeRequest(POST, "/refreshToken").withFormUrlEncodedBody(
        "refresh_token" -> "valid-refresh-token"
      )

      // テストを実行
      val result = controller.refreshToken.apply(request)

      // アサーション
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      val json = contentAsJson(result)
      (json \ "access_token").as[String] mustBe "new-access-token"
      (json \ "id_token").as[String] mustBe "new-id-token"
      (json \ "refresh_token").as[String] mustBe "new-refresh-token"
      (json \ "token_type").as[String] mustBe "bearer"
      (json \ "expires_in").as[Int] mustBe 3600
    }

    "return Unauthorized for an invalid refresh token" in {
      // モックの設定
      val authService = mock[AuthService]
      val userService = mock[UserService]
      val controller  = new AuthController(stubControllerComponents(), authService, userService)

      // モックの振る舞いを設定
      when(authService.refreshToken(any[String])).thenReturn(Future.successful(None))

      // テストするリクエストを作成
      val request = FakeRequest(POST, "/refreshToken").withFormUrlEncodedBody(
        "refresh_token" -> "invalid-refresh-token"
      )

      // テストを実行
      val result = controller.refreshToken.apply(request)

      // アサーション
      status(result) mustBe UNAUTHORIZED
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] mustBe "Invalid or expired refresh token"
    }
  }
}
