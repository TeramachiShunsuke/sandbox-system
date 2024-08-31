package utils

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import scala.util.{Success, Failure}
import java.time.{Clock, ZoneId}
import scala.util.Try

object JwtUtility {
  private val secret = sys.env.getOrElse("JWT_SECRET", "your-256-bit-secret")
  private val algo   = JwtAlgorithm.HS256

  implicit private val clock: Clock = Clock.system(ZoneId.of("Asia/Tokyo"))

  def createToken(userId: String, expirationTime: Long): String = {
    val claim = JwtClaim(
      expiration = Some(System.currentTimeMillis() / 1000 + expirationTime),
      issuedAt = Some(System.currentTimeMillis() / 1000),
      subject = Some(userId)
    ).issuedNow.expiresIn(expirationTime)

    Jwt.encode(claim, secret, algo)
  }

  def createIdToken(userId: String, expirationTime: Long): String = {
    val claim = JwtClaim(
      expiration = Some(System.currentTimeMillis() / 1000 + expirationTime),
      issuedAt = Some(System.currentTimeMillis() / 1000),
      subject = Some(userId)
    ).issuedNow
      .expiresIn(expirationTime)
      .+("id_token", userId) // 正しい形式でカスタムクレームを追加

    Jwt.encode(claim, secret, algo)
  }

  def validateToken(token: String): Try[String] = {
    Jwt
      .decode(token, secret, Seq(algo))
      .map(_.subject.getOrElse(throw new IllegalArgumentException("No subject in token")))
  }
}
