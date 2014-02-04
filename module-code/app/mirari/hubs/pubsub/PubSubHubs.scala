package mirari.hubs.pubsub

import mirari.hubs.Hubs

/**
 * @author alari
 * @since 12/23/13
 */
trait PubSubHubs {
  self: Hubs =>

  /**
   * Helper -- just not to forget Broadcast wrapper
   * @param hub hub name
   * @param topic topic id
   * @param message message
   */
  def broadcast(hub: String, topic: String, message: Any) = this(hub)(topic) ! PubSubTopic.Broadcast(message)

  /**
   * Helper -- forces target hub creation
   * @param hub hub name
   * @param topic topic id
   * @param message message
   */
  def forceBroadcast(hub: String, topic: String, message: Any) = this(hub)(topic) !! PubSubTopic.Broadcast(message)
}
