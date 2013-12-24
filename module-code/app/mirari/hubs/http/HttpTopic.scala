package mirari.hubs.http

import mirari.hubs.HubTopic
import akka.actor.Actor
import mirari.wished.Unwished

/**
 * @author alari
 * @since 12/24/13
 */
trait HttpTopic[T] extends HubTopic[T]{
  topic: Actor =>

  val handleHttpAction: PartialFunction[HttpAction[State],Unit]

  val httpBehaviour: Receive = {
    case ha@HttpAction(_, s: State, _) =>
      handleHttpAction.applyOrElse(ha.asInstanceOf[HttpAction[State]], _ => sender ! Unwished.NotFound)

    case _: HttpAction =>
      sender ! Unwished.NotFound
  }
}
