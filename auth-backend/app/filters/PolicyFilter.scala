package filters

import javax.inject.Inject
import org.apache.pekko.stream.Materializer
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class PolicyFilter @Inject() (implicit
  override val mat: Materializer,
  ec: ExecutionContext
) extends Filter {

  override def apply(
    nextFilter: RequestHeader => Future[Result]
  )(requestHeader: RequestHeader): Future[Result] = {
    nextFilter(requestHeader).map { result =>
      result.withHeaders(
        "Referrer-Policy" -> "strict-origin-when-cross-origin"
      )
    }
  }
}
