package components

import com.raquo.laminar.api.L._
import components.Component

final case class PageHeader(title: String, subtitle: String) extends Component {
  def body: Div =
    div(
      h1(
        cls("text-4xl text-gray-50 font-bold font-mono"),
        title
      ),
      h2(
        cls("text-xl mt-2 text-gray-300 font-mono"),
        span(cls("text-gray-500"), "#"),
        span(subtitle)
      )
    )
}
