package mirari.hubs

import play.api.test.PlaySpecification
import akka.actor._
import play.api.mvc.RequestHeader
import akka.testkit.TestProbe
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Future

/**
 * @author alari (name.alari@gmail.com)
 * @since 25.12.13 15:21
 */
class HubsSpec extends PlaySpecification{
  implicit val system = ActorSystem("hubs-spec")

  val hub = new Hubs(system) with StateHubs[RequestHeader] {
    def state(implicit rh: RequestHeader) = Future successful rh
  }

  class Topic(id: String, probe: ActorRef) extends Actor with HubTopic[RequestHeader] {
    val hubs = hub
    probe.tell(id, self)
    def receive = {
      case m =>
        probe.tell((id, m), sender)
    }
  }

  val probe = TestProbe()
  val sender = TestProbe()
  implicit val s = sender.ref

  hub("a") = (s:String) => Props(new Topic(s, probe.ref))
  val aHub = hub("a")

  "hubs system" should {
    "fail with invalid hub" in {
      hub("b")("c") ? 0 must throwA[Exception].await
    }
    "create a topic" in {
      aHub("t0").check.mapTo[Boolean] must beFalse.await
      aHub("t0") ! "not creating"
      probe.expectNoMsg(FiniteDuration(10, "millisecond"))
      aHub("t0").reach("creating")
      probe.expectMsg( "t0" )
      probe.expectMsg( "t0" -> "creating" )
      aHub("t0").check.mapTo[Boolean] must beTrue.await
    }
  }
}
