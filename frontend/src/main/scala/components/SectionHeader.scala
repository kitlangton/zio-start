package components

import com.raquo.laminar.api.L._

final case class SectionHeader(name: Mod[HtmlElement]) extends Component {
  def body: Div =
    div(
      cls("text-gray-400 font-semibold tracking-widest uppercase mb-2"),
      name
    )
}
