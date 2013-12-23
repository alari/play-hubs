package mirari.hubs

import akka.actor.{OneForOneStrategy, Props, Actor}
import akka.actor.SupervisorStrategy.Restart

/**
 * @author alari
 * @since 12/19/13
 */
private[hubs] class HubsActor extends Actor {

  import HubsActor._

  override val supervisorStrategy = OneForOneStrategy() {
    case _ => Restart
  }

  def receive = {
    case CreateHub(name, props) =>
      sender ! (context.child(name) match {
        case Some(n) => n
        case None => context.actorOf(Props(classOf[HubActor], props), name)
      })

    case HubMessage(hub, message) =>
      context.child(hub).map(_.tell(message, sender)).getOrElse(sender ! new Exception(s"Hub $hub not found"))
  }
}

private[hubs] object HubsActor {

  case class CreateHub(name: String, topicProps: String => Props)

  case class HubMessage(hub: String, message: Any)

}