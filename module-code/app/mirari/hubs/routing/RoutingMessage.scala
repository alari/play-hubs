package mirari.hubs.routing

/**
 * @author alari
 * @since 12/19/13
 */
case class RoutingMessage[T](resource: String, action: String, state: T, data: Option[Any] = None)


