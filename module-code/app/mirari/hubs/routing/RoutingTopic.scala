package mirari.hubs.routing

import akka.actor.Actor

/**
 * Common routing behaviour for topic
 */
trait RoutingTopic[T] {
  actor: Actor =>

  val resourceUrl: String = "/" + self.path.parent.name + "/" + self.path.name

  type State = T

  val routingBehaviour: Actor.Receive = {
    case RoutingMessage(`resourceUrl`, action, state, data) =>
      handleAction(action, state.asInstanceOf[State], data)
  }

  def handleAction(action: String, state: State, data: Option[Any]): Unit

}
