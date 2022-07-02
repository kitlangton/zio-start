package components

import com.raquo.laminar.api.L._

final case class Paragraph(mods: Mod[HtmlElement]*) extends Component {
  def body =
    p(
      cls("mt-4 text-lg"),
      mods
    )
}
