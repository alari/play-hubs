package mirari.hubs

import akka.actor.Actor

/**
 * @author alari
 * @since 12/23/13
 */
trait HubTopic[T] {
  _: Actor =>

  type State = T

  val resourceUrl: String = "/" + self.path.parent.name + "/" + self.path.name
}
