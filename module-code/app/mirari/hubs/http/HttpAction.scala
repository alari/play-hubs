package mirari.hubs.http

import play.api.mvc.Request
import mirari.hubs.Hubs

/**
 * @author alari
 * @since 12/24/13
 */
case class HttpAction(action: String, state: Hubs#State, request: Request[_])
