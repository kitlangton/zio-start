package components

import com.raquo.laminar.api.L._
import zio.start.Router

final case class Link(name: String) extends Component {

  def body = {
    val $active = Router.router.$currentPage.map {
      case Router.BookPage(title, subtitle) if subtitle == name => true
      case _                                                    => false
    }
    span(
      cls("text-mono"),
      cls.toggle("cursor-pointer text-blue-400 hover:text-blue-300") <-- $active.map(!_),
      cls.toggle("text-gray-400 font-medium") <-- $active,
//      span(
//        cls("text-gray-500")
//        color("#3977C3"),
//        "#"
//      ),
      name,
      onClick --> { _ =>
        Router.router.pushState(Router.BookPage("List", name))
      }
    )
  }

}
