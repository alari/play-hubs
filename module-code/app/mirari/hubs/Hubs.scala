package mirari.hubs

import akka.actor._
import akka.pattern.ask
import scala.concurrent.{Await, Future}
import play.api.mvc.RequestHeader
import scala.concurrent.duration.Duration
import mirari.hubs.routing.{RoutingTopic, RoutingHubs}
import mirari.hubs.pubsub.{PubSubClient, PubSubTopic, PubSubHubs}

/**
 * @author alari
 * @since 12/19/13
 */
abstract class Hubs(system: ActorSystem) {
  val guardianName = "hubs"

  val hubs = system.actorOf(Props[HubsActor], guardianName)

  def update(name: String, topicProps: Props) {
    hubs ! HubsActor.CreateHub(name, topicProps)
  }

  def hubFor(name: String, topicProps: Props) = {
    this(name) = topicProps
    this(name)
  }

  val DefaultTimeout = akka.util.Timeout(100)

  /**
   * Hub wrapper for apply() function
   * @param name hub name
   */
  case class Hub(name: String) {
    def apply(topic: String) = Topic(topic)

    case class Topic(topic: String) {
      def reach(message: Any, sender: ActorRef = hubs) = hubs.tell(HubsActor.HubMessage(name, HubActor.ReachTopic(topic, message)), sender)

      def !!(message: Any)(implicit sender: ActorRef = hubs) = reach(message, sender)

      def send(message: Any, sender: ActorRef = hubs) = hubs.tell(HubsActor.HubMessage(name, HubActor.TryTopic(topic, message)), sender)

      def !(message: Any)(implicit sender: ActorRef = hubs) = send(message, sender)

      def retrieve(message: Any)(implicit timeout: akka.util.Timeout = DefaultTimeout) = hubs ? HubsActor.HubMessage(name, HubActor.ReachTopic(topic, message))

      def ?(message: Any)(implicit timeout: akka.util.Timeout = DefaultTimeout) = retrieve(message)

      def check(implicit timeout: akka.util.Timeout = DefaultTimeout) = hubs ? HubsActor.HubMessage(name, HubActor.CheckTopic(topic))
    }

  }

  /**
   * Method to wrap the hub with a name. You could do val myHub = this("my-hub") to cache it
   * @param name name
   * @return
   */
  def apply(name: String): Hubs#Hub = Hub(name).asInstanceOf[Hubs#Hub]

  /**
   * Shortcut: create and access a new hub
   * @param name name
   * @param topicProps hub topic props
   */
  def apply(name: String, topicProps: Props): Hubs#Hub = hubFor(name, topicProps)
}

/**
 * Helper: hubs builder with full functionality
 * @param system actor system to place actors in
 * @tparam T state type
 */
abstract class FullHubs[T](system: ActorSystem) extends Hubs(system) with StateHubs[T] with RoutingHubs[T] with PubSubHubs

/**
 * Helper: full hubs topic to implement
 * @tparam T state
 */
trait FullTopic[T] extends Actor with PubSubTopic[T] with RoutingTopic[T] with UpdateStash {
  def topicId = self.path.name

  def customBehaviour: Receive = {case a => play.api.Logger.debug(s"Unhandled message for ${self.path}: $a")}

  def handleAction(action: String, state: T, data: Option[Any]) {
    play.api.Logger.debug(s"You should implement handleAction(action: String, state: T, data: Option[Any]) for ${getClass.getCanonicalName}")
  }

  def receive = topicBehaviour orElse customBehaviour
}

/**
 * Helper: full hubs client
 * @tparam T state
 */
trait FullClient[T] extends Actor with PubSubClient[T] with UpdateStash