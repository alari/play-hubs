package mirari.hubs.http

import mirari.hubs.HubTopic
import akka.actor.Actor
import mirari.wished.Unwished
import play.api.mvc.Results

/**
 * @author alari
 * @since 12/24/13
 */
trait HttpTopic extends HubTopic with Results {
  topic: Actor =>

  type HttpHandler = PartialFunction[HttpAction, Unit]

  val handleHttpAction: HttpHandler

  val httpBehaviour: Receive = {
    case ha: HttpAction =>
      handleHttpAction.applyOrElse(ha, {
        _: HttpAction => sender ! Unwished.NotFound
      })

  }
}