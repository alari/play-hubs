package mirari.hubs

import akka.actor.{PoisonPill, Stash, Props, Actor}
import scala.concurrent.Future

/**
 * @author alari
 * @since 12/23/13
 */
trait HubTopic[T] {
  _: Actor =>

  def hubs: Hubs with StateHubs[T]

  def topicBehaviour: Receive = {
    case _ if false =>
  }

  val childrenProps: Map[String,Props] = Map()

  def child(name: String) = context.child(name).getOrElse(context.actorOf(childrenProps(name), name))

  val resourceUrl: String = "/" + self.path.parent.name + "/" + self.path.name
}

trait UpdateStash extends Stash{
  _: Actor =>

  import context.dispatcher
  import UpdateStash._

  def update[T](f: Future[T])(found: T => Boolean, notFound: Throwable => Boolean = {_=>true}) {

    context.become({
      case Loaded(t: T) =>
        if(found(t)) {
          context.unbecome()
          unstashAll()
        }

      case Failed(e) =>
        if(notFound(e)) {
          self ! PoisonPill
        }

      case Release =>
        context.unbecome()
        unstashAll()

      case _ =>
        stash()
    })
    f.map {
      h =>
        self ! Loaded(h)
    } recover {
      case e =>
        self ! Failed(e)
    }
  }
}

object UpdateStash {
  case class Loaded[T](t: T)
  case class Failed(ex: Throwable)
  case object Release
}