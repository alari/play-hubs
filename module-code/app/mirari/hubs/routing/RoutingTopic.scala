package mirari.hubs.routing

import akka.actor.Actor
import mirari.hubs.{Hubs, HubTopic}

/**
 * Common routing behaviour for topic
 */
trait RoutingTopic[T] extends HubTopic[T]{
  actor: Actor =>

  val routingBehaviour: Actor.Receive = {
    case RoutingMessage(`resourceUrl`, action, state: T, data) =>
      handleAction(action, state, data)
  }

  def handleAction(action: String, state: T, data: Option[Any]): Unit
}
