import org.apache.pekko.stream.Materializer
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import models.{User, UserForm}
import services.{UserService, AuthService}
import actions.{AuthenticatedAction, AuthenticatedRequest}
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import java.sql.Timestamp
import play.api.http.{HttpErrorHandler, ParserConfiguration}
import play.api.libs.Files.TemporaryFileCreator
import play.api.inject.guice.GuiceApplicationBuilder
import controllers.UserController

class UserControllerSpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with Injecting
    with MockitoSugar
    with ScalaFutures {

  // TestのExecutionContextのためのimplicit宣言
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  // Materializerの取得
  implicit lazy val mat: Materializer = app.materializer

  // Application Builderのオーバーライド
  override def fakeApplication() =
    GuiceApplicationBuilder().build()

  // インスタンスの取得にGuiceを使用
  lazy val tfc    = app.injector.instanceOf[TemporaryFileCreator]
  lazy val eh     = app.injector.instanceOf[HttpErrorHandler]
  lazy val config = app.injector.instanceOf[ParserConfiguration]

  // BodyParsers.Defaultの生成
  lazy val defaultBodyParser = new BodyParsers.Default(tfc, eh, config)

  "UserController" should {

    "return a list of users" in {
      val mockUserService = mock[UserService]
      val cc              = stubControllerComponents()
      val mockAuthService = mock[AuthService]
      when(mockAuthService.validateToken(any[String]))
        .thenReturn(Future.successful(Some("testUserId")))

      val actualAuthAction =
        new AuthenticatedAction(defaultBodyParser, mockAuthService)(cc.executionContext)

      val userList = Seq(
        User(
          id = "537385b5-de3d-4173-a67f-86354387e3f4",
          username = "newuser",
          password = "$2a$10$RtUvI/oPjYUw.K41hu4xZOu4P/iF851tBs.RmUS8NLWR/IvOb41gG",
          email = "newuser@example.com",
          apikey = "",
          isDeleted = false,
          createdAt = Some(Timestamp.valueOf("2024-08-27 10:27:21")),
          updatedAt = Some(Timestamp.valueOf("2024-08-27 10:27:21"))
        )
      )

      when(mockUserService.listAllUsers).thenReturn(Future.successful(userList))

      val controller = new UserController(cc, mockUserService, actualAuthAction)

      val result = controller.listUsers()(
        FakeRequest(GET, "/users").withHeaders("Authorization" -> "Bearer validToken")
      )

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(userList)
    }

    "return Unauthorized when token is invalid" in {
      val mockUserService = mock[UserService]
      val cc              = stubControllerComponents()
      val mockAuthService = mock[AuthService]

      when(mockAuthService.validateToken(any[String])).thenReturn(Future.successful(None))

      val actualAuthAction =
        new AuthenticatedAction(defaultBodyParser, mockAuthService)(cc.executionContext)

      val controller = new UserController(cc, mockUserService, actualAuthAction)

      val result = controller.listUsers()(
        FakeRequest(GET, "/users").withHeaders("Authorization" -> "Bearer invalid_token")
      )

      status(result) mustBe UNAUTHORIZED
      contentAsString(result) mustBe "Invalid token"
    }

    // 特定のユーザーの取得テスト
    "return a specific user by ID" in {
      val mockUserService = mock[UserService]
      val cc              = stubControllerComponents()
      val mockAuthService = mock[AuthService]

      when(mockAuthService.validateToken(any[String]))
        .thenReturn(Future.successful(Some("testUserId")))

      val actualAuthAction =
        new AuthenticatedAction(defaultBodyParser, mockAuthService)(cc.executionContext)

      val user = User("1", "testuser", "hashedpassword", "test@example.com")

      when(mockUserService.getUserById("1")).thenReturn(Future.successful(Some(user)))

      val controller = new UserController(cc, mockUserService, actualAuthAction)

      val result = controller.getUser("1")(
        FakeRequest(GET, "/users/1").withHeaders("Authorization" -> "Bearer validToken")
      )

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(user)
    }

    // 存在しないユーザーの取得テスト
    "return NotFound when user does not exist" in {
      val mockUserService = mock[UserService]
      val cc              = stubControllerComponents()
      val mockAuthService = mock[AuthService]

      when(mockAuthService.validateToken(any[String]))
        .thenReturn(Future.successful(Some("testUserId")))

      val actualAuthAction =
        new AuthenticatedAction(defaultBodyParser, mockAuthService)(cc.executionContext)

      when(mockUserService.getUserById("1")).thenReturn(Future.successful(None))

      val controller = new UserController(cc, mockUserService, actualAuthAction)

      val result = controller.getUser("1")(
        FakeRequest(GET, "/users/1").withHeaders("Authorization" -> "Bearer validToken")
      )

      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe Json.obj("error" -> "User not found")
    }

    // ユーザー作成のテスト
    "create a new user successfully" in {
      val mockUserService = mock[UserService]
      val cc              = stubControllerComponents()
      val mockAuthService = mock[AuthService]

      when(mockAuthService.validateToken(any[String]))
        .thenReturn(Future.successful(Some("testUserId")))

      val actualAuthAction =
        new AuthenticatedAction(defaultBodyParser, mockAuthService)(cc.executionContext)

      val newUserForm = UserForm("newuser", "password123", "newuser@example.com")
      val newUser     = User("2", "newuser", "newuser@example.com", "hashedpassword")

      when(mockUserService.getUserByUsername("newuser")).thenReturn(Future.successful(None))
      when(mockUserService.getUserByEmail("newuser@example.com"))
        .thenReturn(Future.successful(None))
      when(mockUserService.createUser(any[String], any[String], any[String]))
        .thenReturn(Future.successful(newUser))

      val controller = new UserController(cc, mockUserService, actualAuthAction)

      val request = FakeRequest(POST, "/users")
        .withHeaders("Authorization" -> "Bearer validToken")
        .withBody(Json.toJson(newUserForm))

      val result = controller.createUser()(request)

      status(result) mustBe CREATED
      contentAsJson(result) mustBe Json.obj(
        "id"       -> newUser.id,
        "username" -> newUser.username,
        "email"    -> newUser.email
      )
    }

    // ユーザー作成失敗のテスト
    "fail to create a user with invalid input" in {
      val mockUserService = mock[UserService]
      val cc              = stubControllerComponents()
      val mockAuthService = mock[AuthService]

      when(mockAuthService.validateToken(any[String]))
        .thenReturn(Future.successful(Some("testUserId")))

      val actualAuthAction =
        new AuthenticatedAction(defaultBodyParser, mockAuthService)(cc.executionContext)

      // モックの設定 - getUserByUsernameとgetUserByEmailがNoneを返す
      when(mockUserService.getUserByUsername(any[String])).thenReturn(Future.successful(None))
      when(mockUserService.getUserByEmail(any[String])).thenReturn(Future.successful(None))

      // モックの設定 - createUserメソッドが例外をスローする
      when(mockUserService.createUser(any[String], any[String], any[String]))
        .thenReturn(Future.failed(new IllegalArgumentException("Invalid input")))

      val invalidUserFormJson =
        Json.parse("""{"username": "", "password": "short", "email": "invalidemail"}""")

      val controller = new UserController(cc, mockUserService, actualAuthAction)

      val request = FakeRequest(POST, "/users")
        .withHeaders("Authorization" -> "Bearer validToken")
        .withBody(invalidUserFormJson)

      val result = controller.createUser()(request)

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "error").as[String] must include("Invalid input")
    }

    // ユーザーの更新テスト
    "update an existing user successfully" in {
      val mockUserService = mock[UserService]
      val cc              = stubControllerComponents()
      val mockAuthService = mock[AuthService]

      when(mockAuthService.validateToken(any[String]))
        .thenReturn(Future.successful(Some("testUserId")))

      val actualAuthAction =
        new AuthenticatedAction(defaultBodyParser, mockAuthService)(cc.executionContext)

      val updatedUser = User("1", "updateduser", "updated@example.com", "hashedpassword")

      when(mockUserService.updateUser(any[String], any[User]))
        .thenReturn(Future.successful(Some(updatedUser)))

      val controller = new UserController(cc, mockUserService, actualAuthAction)

      val request = FakeRequest(PUT, "/users/1")
        .withHeaders("Authorization" -> "Bearer validToken")
        .withBody(Json.toJson(updatedUser))

      val result = controller.updateUser("1")(request)

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(updatedUser)
    }

    // 存在しないユーザーの更新テスト
    "return NotFound when updating a non-existent user" in {
      val mockUserService = mock[UserService]
      val cc              = stubControllerComponents()
      val mockAuthService = mock[AuthService]

      when(mockAuthService.validateToken(any[String]))
        .thenReturn(Future.successful(Some("testUserId")))

      val actualAuthAction =
        new AuthenticatedAction(defaultBodyParser, mockAuthService)(cc.executionContext)

      when(mockUserService.updateUser(any[String], any[User])).thenReturn(Future.successful(None))

      val controller = new UserController(cc, mockUserService, actualAuthAction)

      val updatedUser = User("1", "updateduser", "updated@example.com", "hashedpassword")

      val request = FakeRequest(PUT, "/users/1")
        .withHeaders("Authorization" -> "Bearer validToken")
        .withBody(Json.toJson(updatedUser))

      val result = controller.updateUser("1")(request)

      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe Json.obj("error" -> "User not found")
    }

    // ユーザー削除のテスト
    "delete a user successfully" in {
      val mockUserService = mock[UserService]
      val cc              = stubControllerComponents()
      val mockAuthService = mock[AuthService]

      when(mockAuthService.validateToken(any[String]))
        .thenReturn(Future.successful(Some("testUserId")))

      val actualAuthAction =
        new AuthenticatedAction(defaultBodyParser, mockAuthService)(cc.executionContext)

      when(mockUserService.deleteUser("1")).thenReturn(Future.successful(Some(())))

      val controller = new UserController(cc, mockUserService, actualAuthAction)

      val result = controller.deleteUser("1")(
        FakeRequest(DELETE, "/users/1").withHeaders("Authorization" -> "Bearer validToken")
      )

      status(result) mustBe NO_CONTENT
    }

    // 存在しないユーザー削除のテスト
    "return NotFound when deleting a non-existent user" in {
      val mockUserService = mock[UserService]
      val cc              = stubControllerComponents()
      val mockAuthService = mock[AuthService]

      when(mockAuthService.validateToken(any[String]))
        .thenReturn(Future.successful(Some("testUserId")))

      val actualAuthAction =
        new AuthenticatedAction(defaultBodyParser, mockAuthService)(cc.executionContext)

      when(mockUserService.deleteUser("1")).thenReturn(Future.successful(None))

      val controller = new UserController(cc, mockUserService, actualAuthAction)

      val result = controller.deleteUser("1")(
        FakeRequest(DELETE, "/users/1").withHeaders("Authorization" -> "Bearer validToken")
      )

      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe Json.obj("error" -> "User not found")
    }

    // APIキーの更新テスト
    "update a user's API key successfully" in {
      val mockUserService = mock[UserService]
      val cc              = stubControllerComponents()
      val mockAuthService = mock[AuthService]

      when(mockAuthService.validateToken(any[String]))
        .thenReturn(Future.successful(Some("testUserId")))

      val actualAuthAction =
        new AuthenticatedAction(defaultBodyParser, mockAuthService)(cc.executionContext)

      when(mockUserService.updateApikey("1", "newapikey"))
        .thenReturn(Future.successful(Some("newapikey")))

      val controller = new UserController(cc, mockUserService, actualAuthAction)

      val request = FakeRequest(PATCH, "/users/1/apikey")
        .withHeaders("Authorization" -> "Bearer validToken")
        .withBody(Json.obj("apikey" -> "newapikey"))

      val result  = controller.updateApikey("1")(request)

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.obj("apikey" -> "newapikey")
    }

    // APIキー更新失敗のテスト
    "fail to update API key when missing from request body" in {
      val mockUserService = mock[UserService]
      val cc              = stubControllerComponents()
      val mockAuthService = mock[AuthService]

      when(mockAuthService.validateToken(any[String]))
        .thenReturn(Future.successful(Some("testUserId")))

      val actualAuthAction =
        new AuthenticatedAction(defaultBodyParser, mockAuthService)(cc.executionContext)

      val controller = new UserController(cc, mockUserService, actualAuthAction)

      val request = FakeRequest(PATCH, "/users/1/apikey")
        .withHeaders("Authorization" -> "Bearer validToken")
        .withBody(Json.obj())

      val result = controller.updateApikey("1")(request)

      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj("error" -> "Missing apikey in request body")
    }
  }
}
