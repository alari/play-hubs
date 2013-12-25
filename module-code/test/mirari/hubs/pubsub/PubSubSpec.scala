package mirari.hubs.pubsub

import play.api.test.{FakeRequest, PlaySpecification}
import akka.actor.{Props, Actor, ActorRef, ActorSystem}
import akka.testkit.TestProbe
import mirari.hubs.{HubTopic, Hubs}
import scala.concurrent.duration.FiniteDuration

/**
 * @author alari (name.alari@gmail.com)
 * @since 25.12.13 16:04
 */
class PubSubSpec extends PlaySpecification {
  implicit val system = ActorSystem("pubsub-spec")

  val hub = new Hubs(system) with PubSubHubs

  class Topic(id: String, probe: ActorRef) extends Actor with HubTopic with PubSubTopic {
    val hubs = hub
    probe.tell(id, self)

    override def timeoutDelay = FiniteDuration(222, "milliseconds")

    def receive = pubSubBehaviour orElse ({
      case m =>
        play.api.Logger.error(m.toString)
        probe.tell((id, m), sender)
    }: Receive)

    val canSubscribe: CanSubscribe = {
      case _ => true
    }
  }

  val probe = TestProbe()
  val sender = TestProbe()
  implicit val s = sender.ref

  hub("a") = (s: String) => Props(new Topic(s, probe.ref))
  val aHub = hub("a")

  "pubsub hubs system" should {
    "subscribe, leave, and timeout" in {
      aHub("t0") !! PubSubTopic.Join(FakeRequest())

      probe.expectMsg("t0")

      hub.broadcast("a", "t0", "1")

      sender.expectMsg("1")

      aHub("t0").check.mapTo[Boolean] must beTrue.await

      aHub("t0") ! PubSubTopic.Leave

      aHub("t0").check.mapTo[Boolean] must beTrue.await

      hub.broadcast("a", "t0", "1")

      sender.expectNoMsg()

      aHub("t0").check.mapTo[Boolean] must beFalse.await
    }
  }
}
