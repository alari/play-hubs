package mirari.hubs

import akka.actor._
import akka.pattern.ask
import scala.concurrent.{Await, Future}
import play.api.mvc.RequestHeader
import scala.concurrent.duration.Duration

/**
 * @author alari
 * @since 12/19/13
 */
abstract class Hubs(system: ActorSystem) {
  type State

  def state(implicit rh: RequestHeader): Future[State]
  def stateSync(implicit rh: RequestHeader): State = Await.result(state, Duration(1, "second"))

  val guardianName = "hubs"

  val hubs = system.actorOf(Props[HubsActor], guardianName)

  def update(name: String, topicProps: String => Props) {
    hubs ! HubsActor.CreateHub(name, topicProps)
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

      def !!(message: Any)(implicit sender: ActorRef) = reach(message, sender)

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
}