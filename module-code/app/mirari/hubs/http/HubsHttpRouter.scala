package mirari.hubs.http

import play.api.mvc._
import play.core.Router
import scala.runtime.AbstractPartialFunction
import mirari.hubs.Hubs
import scala.concurrent.{Future, ExecutionContext}
import mirari.wished.{Unwished, WishedAction}

/**
 * @author alari
 * @since 12/24/13
 */
abstract class HubsHttpRouter[T](hubs: Hubs, ec: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext) extends Router.Routes with BodyParsers with Results {
  private var path: String = ""

  type State = T

  /**
   * Builds user state by a header -- e.g. authenticates the user
   * @param rh request header
   * @return
   */
  def state(rh: RequestHeader): Future[State]

  /**
   * Used by play
   * @param prefix url prefix
   */
  def setPrefix(prefix: String) {
    path = prefix
  }

  /**
   * Used by play
   * @return
   */
  def prefix = path

  /**
   * Used by play
   * @return
   */
  def documentation = Nil

  /**
   * User could override this val to parse certain requests special way
   */
  val parsers: PartialFunction[RequestHeader, BodyParser[_]] = {
    case _ if false => null
  }

  /**
   * Returns user-defined body parser or a default one -- "empty" for get/options, "anyContent" for other methods
   * @param r
   * @return
   */
  private def bodyParser(r: RequestHeader) = {
    def defaultBodyParser(rr: RequestHeader) =
      if(rr.method == "GET" || rr.method == "OPTIONS") parse.empty
      else parse.anyContent
    parsers.applyOrElse(r, defaultBodyParser)
  }

  /**
   * Url regexp to handle with router: /:hub/:topic/?action?
   */
  private val urlParse = "/([^/]+)/([^/]+)(/(.*))?".r

  def routes = new AbstractPartialFunction[RequestHeader, Handler] {

    override def applyOrElse[A <: RequestHeader, B >: Handler](rh: A, default: A => B) = {
      if (rh.path.startsWith(path)) {
        rh.path.drop(path.length) match {
          case urlParse(hub, topic, _, action) =>
            handler(bodyParser(rh), hub, topic, action)
          case _ => default(rh)
        }
      } else {
        default(rh)
      }
    }

    def isDefinedAt(rh: RequestHeader) = rh.path.startsWith(path)
  }

  /**
   * Produces a handler for the given properties
   * @param parser body parser to use
   * @param hub hub id
   * @param topic topic id
   * @param action action subline
   * @return handler
   */
  private def handler(parser: BodyParser[_], hub: String, topic: String, action: String): Handler = WishedAction.async(parser) {
    request =>
      for {
        s <- state(request)
        resp <- hubs(hub)(topic) ? HttpAction(action, s, request)
      } yield resp match {
        case res: SimpleResult => res
        case u: Unwished[_] => u.response
        case z =>
          play.api.Logger.error(s"hub = $hub; topic = $topic; action = $action; returned not a simple result but this: $z")
          InternalServerError
      }
  }
}
