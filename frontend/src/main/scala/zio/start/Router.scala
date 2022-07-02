package zio.start

import boopickle.Default._
import com.raquo.laminar.api.L._
import com.raquo.waypoint._

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import scala.scalajs.js

object Router {

  sealed trait Page
  case class BookPage(title: String, subtitle: String) extends Page
  case object HomePage                                 extends Page

  val pageRoute: Route[BookPage, (String, String)] = Route(
    encode = userPage => userPage.title -> userPage.subtitle,
    decode = args => BookPage(args._1, args._2),
    pattern = root / segment[String] / segment[String] / endOfSegments
  )

  val homeRoute: Route[HomePage.type, Unit] = Route.static(HomePage, root / endOfSegments)

  val router = new Router[Page](
    routes = List(homeRoute, pageRoute),
    getPageTitle = {
      case HomePage                  => "Scala School"
      case BookPage(title, subtitle) => s"Scala School — $title#$subtitle"
    },
    serializePage = { page =>
      val buffer             = Pickle.intoBytes(page)
      val array: Array[Byte] = Array.ofDim(buffer.remaining())
      buffer.get(array)
      new String(array, StandardCharsets.UTF_8)
    },
    deserializePage = { pageStr =>
      val result =
        Unpickle[Page].fromBytes(ByteBuffer.wrap(pageStr.getBytes(StandardCharsets.UTF_8))) // deserialize the above
      if (js.isUndefined(result) || result == null) HomePage
      else result
    }
  )(
    $popStateEvent = windowEvents.onPopState, // this is how Waypoint avoids an explicit dependency on Laminar
    owner = unsafeWindowOwner                 // this router will live as long as the window
  )
}
