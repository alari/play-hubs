package infra.hubs

import play.api.test.{FakeRequest, PlaySpecification}
import akka.actor.{Props, ActorRef, ActorSystem}
import akka.testkit.TestProbe
import scala.concurrent.duration.FiniteDuration
import play.api.mvc.RequestHeader
import scala.concurrent.Future
import infra.hubs.pubsub.{PubSubTopic, PubSubHubs}

/**
 * @author alari (name.alari@gmail.com)
 * @since 25.12.13 16:04
 */
class PubSubSpec extends PlaySpecification {
  implicit val system = ActorSystem("pubsub-spec")

  val hub = new Hubs(system) with PubSubHubs with StateHubs[RequestHeader] {
    def state(implicit rh: RequestHeader) = Future successful rh
  }

  class Topic(probe: ActorRef) extends FullTopic[RequestHeader] {
    val hubs = hub
    probe.tell(topicId, self)

    override def timeoutDelay = FiniteDuration(222, "milliseconds")

    override def customBehaviour = {
      case m =>
        play.api.Logger.error(m.toString)
        probe.tell((topicId, m), sender)
    }

    val canSubscribe: CanSubscribe = {
      case _ => true
    }
  }

  val probe = TestProbe()
  val sender = TestProbe()
  implicit val s = sender.ref

  hub("a") = Props[Topic]({
    new Topic(probe.ref)
  })
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
