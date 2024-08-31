package services

import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import models.{User, Users}
import javax.inject.{Inject, Singleton}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{ExecutionContext, Future}
import org.mindrot.jbcrypt.BCrypt
import play.api.Logging

@Singleton
class UserService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit
  ec: ExecutionContext
) extends Logging { // ロギングのためにLoggingを追加
  private val db = dbConfigProvider.get[JdbcProfile].db

  def listAllUsers: Future[Seq[User]] = {
    db.run(Users.query.filter(_.isDeleted === false).result)
  }

  def getUserById(id: String): Future[Option[User]] = {
    db.run(Users.query.filter(user => user.id === id && user.isDeleted === false).result.headOption)
  }

  def createUser(username: String, password: String, email: String): Future[User] = {
    val newPassword = hashPassword(password) // ハッシュ化処理を専用メソッドに分離
    val newUser     = User(username = username, password = newPassword, email = email)
    db.run(Users.query += newUser).map(_ => newUser)
  }

  def updateUser(id: String, userData: User): Future[Option[User]] = {
    val query        = Users.query.filter(user => user.id === id && user.isDeleted === false)
    val updateAction = query
      .map(u => (u.username, u.email, u.apikey))
      .update((userData.username, userData.email, userData.apikey))
    db.run(updateAction).flatMap {
      case 0 => Future.successful(None)
      case _ => getUserById(id)
    }
  }

  def deleteUser(id: String): Future[Option[Unit]] = {
    val query = Users.query.filter(_.id === id).map(_.isDeleted).update(true)
    db.run(query).map {
      case 0 => None
      case _ => Some(())
    }
  }

  def updateApikey(id: String, newApikey: String): Future[Option[String]] = {
    val query        = Users.query.filter(user => user.id === id && user.isDeleted === false)
    val updateAction = query.map(_.apikey).update(newApikey)
    db.run(updateAction).flatMap {
      case 0 => Future.successful(None)
      case _ => Future.successful(Some(newApikey))
    }
  }

  def getUserByUsername(username: String): Future[Option[User]] = {
    db.run(
      Users.query
        .filter(user => user.username === username && user.isDeleted === false)
        .result
        .headOption
    )
  }

  def getUserByEmail(email: String): Future[Option[User]] = {
    db.run(
      Users.query.filter(user => user.email === email && user.isDeleted === false).result.headOption
    )
  }

  // パスワードのハッシュ化メソッド
  private def hashPassword(password: String): String = {
    try {
      BCrypt.hashpw(password, BCrypt.gensalt(12)) // コストパラメータは12に設定
    } catch {
      case e: IllegalArgumentException =>
        logger.error("Error hashing password: Invalid salt revision", e)
        throw e // エラーを投げて、エラーハンドリングを行うか再度適切に処理
    }
  }

  def checkPassword(candidate: String, hashed: String): Boolean = {
    try {
      // ハッシュのデバッグログ
      logger.debug(
        s"Checking password: candidate password length: ${candidate.length}, hashed: $hashed"
      )
      BCrypt.checkpw(candidate, hashed)
    } catch {
      case e: IllegalArgumentException =>
        logger.error("Error checking password: Invalid salt revision", e)
        false
    }
  }

  def authenticateUser(username: String, password: String): Future[Option[User]] = {
    getUserByUsername(username).map {
      case Some(user) =>
        // デバッグログでユーザーの情報を確認
        logger.debug(s"Authenticating user: $username with stored hash: ${user.password}")
        if (checkPassword(password, user.password)) Some(user) else None
      case None       =>
        None
    }
  }
}
