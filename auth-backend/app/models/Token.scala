package models

import slick.jdbc.MySQLProfile.api._
import slick.lifted.{ProvenShape, Tag}
import java.sql.Timestamp

case class Token(
  id: String,
  userId: String,
  accessToken: String,
  idToken: Option[String] = None,
  refreshToken: Option[String] = None,
  createdAt: Timestamp,
  expiresAt: Timestamp,
  isRevoked: Boolean = false
)

class Tokens(tag: Tag) extends Table[Token](tag, "tokens") {
  def id           = column[String]("id", O.PrimaryKey)
  def userId       = column[String]("user_id")
  def accessToken  = column[String]("access_token")
  def idToken      = column[Option[String]]("id_token")
  def refreshToken = column[Option[String]]("refresh_token")
  def createdAt    = column[Timestamp]("created_at")
  def expiresAt    = column[Timestamp]("expires_at")
  def isRevoked    = column[Boolean]("is_revoked", O.Default(false))

  def * : ProvenShape[Token] =
    (
      id,
      userId,
      accessToken,
      idToken,
      refreshToken,
      createdAt,
      expiresAt,
      isRevoked
    ) <> (Token.tupled, Token.unapply)
}

object Tokens {
  val query = TableQuery[Tokens]
}
