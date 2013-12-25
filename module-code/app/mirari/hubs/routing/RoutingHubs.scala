package mirari.hubs.routing

import mirari.hubs.{StateHubs, Hubs}
import akka.actor.ActorRef
import scala.concurrent.Future

/**
  * PibSubHubs mixin for user messages handling
  */
trait RoutingHubs[T] {
   self: Hubs with StateHubs[T] =>

   val routeR = "/?([^/]+)/([^/]+)".r

   /**
    * Reach the message to a topic (create it's actor if it doesn't exist)
    * @param msg message to handle
    * @param sender sender
    */
   def reach(msg: RoutingMessage[T], sender: ActorRef = hubs) = msg.resource match {
     case routeR(hub, topic) =>
       apply(hub)(topic).reach(msg, sender)
   }

   /**
    * Reach the message to a topic (create it's actor if it doesn't exist)
    * @param msg message to handle
    * @param sender sender
    */
   def !!(msg: RoutingMessage[T])(implicit sender: ActorRef = hubs): Unit = reach(msg, sender)

   /**
    * Send-and-forget a message to a topic. Message is lost when the topic doesn't exist
    * @param msg message to send
    * @param sender sender
    */
   def send(msg: RoutingMessage[T], sender: ActorRef = hubs) = msg.resource match {
     case routeR(hub, topic) =>
       apply(hub)(topic).send(msg, sender)
   }

   /**
    * Alias for send. Message is lost when the topic doesn't exist
    * @param msg message to send
    * @param sender sender
    */
   def !(msg: RoutingMessage[T])(implicit sender: ActorRef = hubs) = send(msg, sender)

   /**
    * Ask and return a future. Topic will be created if it doesn't exist
    * @param msg message to handle by topic
    * @param timeout timeout
    * @return
    */
   def retrieve(msg: RoutingMessage[T], timeout: akka.util.Timeout = DefaultTimeout): Future[Any] = msg.resource match {
     case routeR(hub, topic, _, act) =>
       apply(hub)(topic).retrieve(msg)
   }

   /**
    * Ask a topic. It will be created if yet doesn't exist
    * @param msg message to handle by topic
    * @param timeout timeout
    * @return
    */
   def ?(msg: RoutingMessage[T])(implicit timeout: akka.util.Timeout = DefaultTimeout) = retrieve(msg, timeout)
 }
