package infra.hubs

import play.api.mvc.RequestHeader
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

/**
 * @author alari (name.alari@gmail.com)
 * @since 25.12.13 17:10
 */
trait StateHubs[T] {
  self: Hubs =>

  type State = T

  def state(implicit rh: RequestHeader): Future[State]

  def stateSync(implicit rh: RequestHeader): State = Await.result(state, Duration(1, "second"))
}
