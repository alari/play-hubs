package mirari.hubs.routing

import akka.actor.Actor

/**
 * Common routing behaviour for topic
 */
trait RoutingTopic[T] {
  self: Actor =>

  type State = T

  val routingBehaviour: Actor.Receive = {
    case RoutingMessage(_, action, state, data) =>
      handleAction(action, state.asInstanceOf[State], data)
  }

  def handleAction(action: String, state: State, data: Option[Any]): Unit

}
