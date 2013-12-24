package mirari.hubs.http

import mirari.hubs.HubTopic
import akka.actor.Actor
import mirari.wished.Unwished
import play.api.mvc.Results

/**
 * @author alari
 * @since 12/24/13
 */
trait HttpTopic[T] extends HubTopic[T] with Results{
  topic: Actor =>

  type HttpHandler = PartialFunction[HttpAction[State],Unit]

  val handleHttpAction: HttpHandler

  val httpBehaviour: Receive = {
    case ha@HttpAction(_, s, _) => s match {
        // TODO: this check is type-erased; how to match it?
      case _: State =>
        handleHttpAction.applyOrElse(ha.asInstanceOf[HttpAction[State]], {_: HttpAction[_] => sender ! Unwished.NotFound})
      case _ =>
        sender ! Unwished.NotFound
    }

    case _: HttpAction[_] =>
      sender ! Unwished.NotFound
  }
}