package mirari.hubs.http

import play.api.mvc.Request

/**
 * @author alari (name.alari@gmail.com)
 * @since 25.12.13 16:47
 */
case class HttpAction[T](action: String, state: T, request: Request[_])
