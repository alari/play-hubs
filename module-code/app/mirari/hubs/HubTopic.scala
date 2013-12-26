package mirari.hubs

import akka.actor.Actor

/**
 * @author alari
 * @since 12/23/13
 */
trait HubTopic[T] {
  _: Actor =>

  def hubs: Hubs with StateHubs[T]

  def topicBehaviour: Receive = {
    case _ if false =>
  }

  val resourceUrl: String = "/" + self.path.parent.name + "/" + self.path.name
}
