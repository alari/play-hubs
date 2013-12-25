package mirari.hubs.http

import play.api.mvc._
import play.core.Router
import scala.runtime.AbstractPartialFunction
import mirari.hubs.Hubs
import scala.concurrent.{Future, ExecutionContext}
import mirari.wished.{Unwished, WishedAction}
import play.api.libs.json.JsValue

/**
 * @author alari
 * @since 12/24/13
 */

/**
 * Router for all hubs system
 *
 * Usage:
 *
 * ->        /api        your.package.SubClassRouterObject
 *
 * @param hubs your hubs system
 * @param ec execution context to handle io in
 */
abstract class HubsHttpRouter(hubs: Hubs, ec: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext) extends HttpRouter {

  implicit val ctx = ec

  /**
   * Url regexp to handle with router: /:hub/:topic/?action?
   */
  private val urlParse = "/([^/]+)/([^/]+)(/(.*))?".r

  override val resourceHandler: RHandler = {
    case urlParse(hub, topic, _, action) =>
      handler(hubs(hub), topic, action)
  }
}

/**
 * Router for a single hub
 *
 * usage:
 * ->    /api/hubName     your.package.SubClassRouterObject
 *
 * @param hubs your hubs system
 * @param hub hub name to handle with this router
 * @param ec execution context for io
 */
abstract class HubHttpRouter(val hubs: Hubs, hub: String, ec: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext) extends HttpRouter {

  implicit val ctx = ec

  private val hubInst: Hubs#Hub = hubs(hub)

  /**
   * Url regexp to handle with router: /:topic/?action?
   */
  private val urlParse = "/([^/]+)(/(.*))?".r

  override val resourceHandler: RHandler = {
    case urlParse(topic, _, action) =>
      handler(hubInst, topic, action)
  }
}

/**
 * Gets topic value from request header
 * @param hubs your hubs system
 * @param hub hub name
 * @param ec execution context for io
 */
abstract class RequestHeaderTopicHttpRouter(val hubs: Hubs, hub: String, ec: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext) extends HttpRouter {

  implicit val ctx = ec

  private val hubInst = hubs(hub)

  def topic(rh: RequestHeader): String

  /**
   * Url regexp to handle with router: /:topic/?action?
   */
  private val urlParse = "(/([^/]+))?".r

  override val resourceHandler: RHandler = ???

  override def routes = new AbstractPartialFunction[RequestHeader, Handler] {

    override def applyOrElse[A <: RequestHeader, B >: Handler](rh: A, default: A => B) = {
      if (rh.path.startsWith(path)) {
        rh.path.drop(path.length) match {
          case urlParse(_, action) =>
            handler(hubInst, topic(rh), action)(bodyParser(rh))
          case _ =>
            default(rh)
        }
      } else {
        default(rh)
      }
    }

    def isDefinedAt(rh: RequestHeader) = rh.path.startsWith(path)
  }
}

/**
 * Router template
 */
private[http] trait HttpRouter extends Router.Routes with BodyParsers with Results {
  private[http] var path: String = ""

  def hubs: Hubs

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
  private[http] def bodyParser(r: RequestHeader) = {
    def defaultBodyParser(rr: RequestHeader) =
      if (rr.method == "GET" || rr.method == "OPTIONS") parse.empty
      else parse.anyContent
    parsers.applyOrElse(r, defaultBodyParser)
  }

  implicit val ctx: ExecutionContext

  type RHandler = PartialFunction[String, BodyParser[_] => Handler]

  val resourceHandler: RHandler

  def routes = new AbstractPartialFunction[RequestHeader, Handler] {

    override def applyOrElse[A <: RequestHeader, B >: Handler](rh: A, default: A => B) = {
      if (rh.path.startsWith(path)) {
        resourceHandler.applyOrElse(rh.path.drop(path.length), {
          _: String => _: BodyParser[_] => default(rh)
        })(bodyParser(rh))
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
  private[http] def handler(hub: Hubs#Hub, topic: String, action: String)(parser: BodyParser[_]): Handler = WishedAction.async(parser) {
    request =>
      for {
        s <- hubs.state(request)
        resp <- hub(topic) ? HttpAction(action, s, request)
      } yield resp match {
        case res: SimpleResult => res
        case u: Unwished[_] => u.response
        case j: JsValue => Ok(j)
        case Some(j: JsValue) => Ok(j)
        case None => NotFound
        case z =>
          play.api.Logger.error(s"hub = $hub; topic = $topic; action = $action; returned not a simple result but this: $z")
          InternalServerError
      }
  }
}