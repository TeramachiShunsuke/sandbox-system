package services

import slick.jdbc.JdbcProfile
import models.{Token, Tokens}
import play.api.db.slick.DatabaseConfigProvider
import utils.JwtUtility
import slick.jdbc.MySQLProfile.api._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import java.sql.Timestamp
import java.util.UUID
import scala.util.{Success, Failure}

class AuthService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit
  ec: ExecutionContext
) {
  private val db = dbConfigProvider.get[JdbcProfile].db

  def createTokens(
    userId: String,
    accessTokenExpiry: Long,
    refreshTokenExpiry: Long
  ): Future[Token] = {
    val accessToken  = JwtUtility.createToken(userId, accessTokenExpiry)
    val idToken      = Some(JwtUtility.createIdToken(userId, accessTokenExpiry))
    val refreshToken = Some(JwtUtility.createToken(userId, refreshTokenExpiry))

    val now       = new Timestamp(System.currentTimeMillis())
    val expiresAt = new Timestamp(System.currentTimeMillis() + accessTokenExpiry * 1000)

    val token = Token(
      id = UUID.randomUUID().toString,
      userId = userId,
      accessToken = accessToken,
      idToken = idToken,
      refreshToken = refreshToken,
      createdAt = now,
      expiresAt = expiresAt
    )

    db.run(Tokens.query += token).map(_ => token)
  }

  def validateToken(tokenValue: String): Future[Option[String]] = {
    JwtUtility.validateToken(tokenValue) match {
      case Success(userId) =>
        db.run(Tokens.query.filter(_.accessToken === tokenValue).result.headOption).map {
          case Some(token)
              if !token.isRevoked && token.expiresAt
                .after(new Timestamp(System.currentTimeMillis())) =>
            Some(userId)
          case _ =>
            None
        }
      case Failure(_)      =>
        Future.successful(None)
    }
  }

  def refreshToken(oldTokenValue: String): Future[Option[Token]] = {
    db.run(Tokens.query.filter(_.refreshToken === oldTokenValue).result.headOption).flatMap {
      case Some(token)
          if !token.isRevoked && token.expiresAt.after(new Timestamp(System.currentTimeMillis())) =>
        createTokens(token.userId, 3600, 7200).map(Some(_))
      case _ =>
        Future.successful(None)
    }
  }
}
