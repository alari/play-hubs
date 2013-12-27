package mirari.hubs.routing

import akka.actor.Actor
import mirari.hubs.HubTopic

/**
 * Common routing behaviour for topic
 */
trait RoutingTopic[T] extends HubTopic[T] {
  actor: Actor =>

  abstract override def topicBehaviour: Receive = super.topicBehaviour orElse routingBehaviour

  val childrenResourceR = "([^/]+)(/(.+))?".r

  val routingBehaviour: Actor.Receive = {
    case RoutingMessage(`resourceUrl`, action, state: T, data) =>
      handleAction(action, state, data)
    case rm@RoutingMessage(_, childrenResourceR(c, _, ac), _, _) if childrenProps.contains(c) =>
      child(c) ! rm.copy(action = ac)
  }

  def handleAction(action: String, state: T, data: Option[Any]): Unit
}
