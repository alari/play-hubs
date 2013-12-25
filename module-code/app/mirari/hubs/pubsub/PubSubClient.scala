package mirari.hubs.pubsub

import akka.actor.Actor
import mirari.hubs.Hubs

/**
  * @author alari
  * @since 12/19/13
  */
trait PubSubClient {
   current: Actor =>
     def state: Hubs#State
     def hubs: Hubs

     def join(hub: String, topic: String) = hubs(hub)(topic) !! PubSubTopic.Join(state)
     def leave(hub: String, topic: String) = hubs(hub)(topic) ! PubSubTopic.Leave
     def broadcast(hub: String, topic: String, message: Any) = hubs(hub)(topic) ! PubSubTopic.Broadcast(message)
 }
