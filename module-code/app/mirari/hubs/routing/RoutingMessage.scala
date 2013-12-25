package mirari.hubs.routing

import mirari.hubs.Hubs

/**
 * @author alari
 * @since 12/19/13
 */
case class RoutingMessage(resource: String, action: String, state: Hubs#State, data: Option[Any] = None)


