package mirari.hubs.routing

import akka.actor.Actor
import mirari.hubs.{Hubs, HubTopic}

/**
 * Common routing behaviour for topic
 */
trait RoutingTopic extends HubTopic{
  actor: Actor =>

  val routingBehaviour: Actor.Receive = {
    case RoutingMessage(`resourceUrl`, action, state, data) =>
      handleAction(action, state, data)
  }

  def handleAction(action: String, state: Hubs#State, data: Option[Any]): Unit
}
