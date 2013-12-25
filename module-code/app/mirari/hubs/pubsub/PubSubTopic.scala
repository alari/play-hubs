package mirari.hubs.pubsub

import akka.actor._
import scala.concurrent.duration.FiniteDuration
import scala.Some
import mirari.hubs.{Hubs, HubTopic}

/**
  * @author alari
  * @since 12/19/13
  */
trait PubSubTopic extends HubTopic{
   topic: Actor =>

   private var listeners = Set[ActorRef]()
   private var timeout: Option[Cancellable] = None

   val timeoutDelay = FiniteDuration(10, "seconds")
  /**
   * Should actor die when there's no listeners connected or not
   */
   val timeoutsEnabled = true

   protected def actualListeners = listeners

  /**
   * Clears or launches timeout
   */
   def checkTimeout() {
     if (listeners.isEmpty) launchTimeout()
     else clearTimeout()
   }

   def clearTimeout() {
     timeout.map(_.cancel())
     timeout = None
   }

   def launchTimeout() {
     import context.dispatcher
     if (timeout.isEmpty && timeoutsEnabled) timeout = Some(context.system.scheduler.scheduleOnce(timeoutDelay, self, PoisonPill))
   }

   launchTimeout()

   def join(actor: ActorRef) {
     listeners += actor
     context.watch(actor)
     clearTimeout()
   }

   def leave(actor: ActorRef) {
     listeners -= actor
     context.unwatch(actor)
     checkTimeout()
   }

   def broadcast(message: Any) {
     listeners.foreach(_ ! message)
   }

   val canSubscribe: PartialFunction[Hubs#State,Boolean]

   val pubSubBehaviour: Receive = {
     case Terminated(a) if listeners.contains(a) =>
       leave(a)

     case PubSubTopic.Broadcast(m) =>
       broadcast(m)

     case PubSubTopic.Join(state) if canSubscribe.applyOrElse(state.asInstanceOf[Hubs#State], {_: Hubs#State => false: Boolean}) =>
       join(sender)

     case PubSubTopic.Leave if listeners.contains(sender) =>
       leave(sender)
   }
 }

object PubSubTopic {

  /**
   * Broadcast the message to all topic listeners
   * @param message message
   */
  case class Broadcast(message: Any)

  /**
   * Subscribe to a topic, if permitted
   * @param listenerState state to check permissions against
   */
  case class Join(listenerState: Any)

  /**
   * Ubsubscribe the sender
   */
  case object Leave

}