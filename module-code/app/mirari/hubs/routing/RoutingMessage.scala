package mirari.hubs.routing

/**
 * @author alari
 * @since 12/19/13
 */
case class RoutingMessage(resource: String, action: String, state: Any, data: Option[Any] = None)


