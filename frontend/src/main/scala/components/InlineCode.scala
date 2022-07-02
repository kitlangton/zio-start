package components

import components.Component
import com.raquo.laminar.api.L._

final case class InlineCode(code: String) extends Component {
  def body =
    pre(
      cls(s"text-pink-300 font-mono inline"),
      code
    )
}
