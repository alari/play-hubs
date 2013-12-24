package mirari.hubs.http

import play.api.mvc.Request

/**
 * @author alari
 * @since 12/24/13
 */
case class HttpAction[T](action: String, state: T, request: Request[_])
