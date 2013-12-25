package mirari.hubs.http

import mirari.hubs.HubTopic
import akka.actor.Actor
import mirari.wished.Unwished
import play.api.mvc.Results

/**
 * @author alari
 * @since 12/24/13
 */
trait HttpTopic[T] extends HubTopic[T] with Results {
  topic: Actor =>

  type HttpHandler = PartialFunction[HttpAction[T], Unit]

  val handleHttpAction: HttpHandler

  val httpBehaviour: Receive = {
    case ha: HttpAction[T] =>
      handleHttpAction.applyOrElse(ha, {
        _: HttpAction[T] => sender ! Unwished.NotFound
      })

  }
}