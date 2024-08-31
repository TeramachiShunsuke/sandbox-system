package models

import slick.jdbc.MySQLProfile.api._
import slick.lifted.{ProvenShape, Tag}
import java.sql.Timestamp
import java.util.UUID
import play.api.libs.json.{Json, OFormat, Format, JsResult, JsValue, JsString}
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import play.api.libs.json._

case class User(
  id: String = UUID.randomUUID().toString,
  username: String,
  password: String,
  email: String,
  apikey: String = "",
  isDeleted: Boolean = false,
  createdAt: Option[Timestamp] = Some(new Timestamp(System.currentTimeMillis())),
  updatedAt: Option[Timestamp] = Some(new Timestamp(System.currentTimeMillis()))
)

object User {
  // Timestampのためのカスタムフォーマット
  implicit val timestampFormat: Format[Timestamp] = new Format[Timestamp] {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    override def reads(json: JsValue): JsResult[Timestamp] =
      json.validate[String].map { str =>
        Timestamp.valueOf(LocalDateTime.parse(str, formatter))
      }

    override def writes(ts: Timestamp): JsValue = JsString(ts.toLocalDateTime.format(formatter))
  }

  // UserのJSONフォーマット
  implicit val userFormat: OFormat[User] = Json.format[User]
}
class Users(tag: Tag) extends Table[User](tag, "users") {
  def id        = column[String]("id", O.PrimaryKey)
  def username  = column[String]("username")
  def password  = column[String]("password")
  def email     = column[String]("email")
  def apikey    = column[String]("apikey")
  def isDeleted = column[Boolean]("is_deleted", O.Default(false))
  def createdAt = column[Option[Timestamp]]("created_at", O.Default(None))
  def updatedAt = column[Option[Timestamp]]("updated_at", O.Default(None))

  def * : ProvenShape[User] =
    (
      id,
      username,
      password,
      email,
      apikey,
      isDeleted,
      createdAt,
      updatedAt
    ) <> ((User.apply _).tupled, User.unapply)
}

object Users {
  val query = TableQuery[Users]
}

case class UserForm(username: String, password: String, email: String)

object UserForm {
  implicit val format: OFormat[UserForm] = Json.format[UserForm]
}
