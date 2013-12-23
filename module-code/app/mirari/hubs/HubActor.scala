package mirari.hubs

import akka.actor.{OneForOneStrategy, Actor, Props}
import akka.actor.SupervisorStrategy.Stop

/**
 * @author alari
 * @since 12/19/13
 */
private[hubs] class HubActor(topicProps: String => Props) extends Actor {

  import HubActor._

  override val supervisorStrategy = OneForOneStrategy(){
    case _ => Stop
  }

  def receive = {
    case CheckTopic(name) =>
      sender ! context.child(name).isDefined

    case ReachTopic(name, message) =>
      (context.child(name) match {
        case Some(a) => a
        case None => context.actorOf(topicProps(name), name)
      }).tell(message, sender)

    case TryTopic(name, message) =>
      context.child(name).map(_.tell(message, sender))
  }
}

private[hubs] object HubActor {

  /**
   * A wrapper for a message to the topic. Creates topic actor if it doesn't exist
   *
   * @param name topic name
   * @param message message
   */
  case class ReachTopic(name: String, message: Any)

  /**
   * A wrapper for a message to the topic. Message will be lost if the topic doesn't exist
   * @param name topic name
   * @param message message
   */
  case class TryTopic(name: String, message: Any)

  /**
   * Checks whether topic exists or not
   * @param name topic name
   */
  case class CheckTopic(name: String)

}