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

  abstract override def topicBehaviour: Receive = super.topicBehaviour orElse httpBehaviour

  type HttpHandler = PartialFunction[HttpAction[T], Unit]

  val handleHttpAction: HttpHandler

  private val childrenActionR = "/([^/]+)(/(.+))?".r

  val httpBehaviour: Receive = {
    case ha: HttpAction[T] =>
      handleHttpAction.applyOrElse(ha, {
        h: HttpAction[T] =>
          h.action match {
            case childrenActionR(c, _, a) if childrenProps.contains(c) =>
              child(c).tell(h.copy(action = a), sender)
            case _ =>
              sender ! Unwished.NotFound
          }
      })

  }
}