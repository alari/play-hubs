package mirari.hubs.routing

import akka.actor.Actor
import mirari.hubs.HubTopic

/**
 * Common routing behaviour for topic
 */
trait RoutingTopic[T] extends HubTopic[T]{
  actor: Actor =>

  val routingBehaviour: Actor.Receive = {
    case RoutingMessage(`resourceUrl`, action, state, data) =>
      handleAction(action, state.asInstanceOf[State], data)
  }

  def handleAction(action: String, state: State, data: Option[Any]): Unit
}
