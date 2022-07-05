package components

import components.Component
import highlight.Highlight
import com.raquo.laminar.api.L._

final case class HighlightedCode(codeString: String, lang: String) extends Component {
  def body =
    pre(
      code(
        onMountCallback { el =>
          val result = Highlight.highlight(lang, codeString.trim).value
          el.thisNode.ref.innerHTML = result
        }
      ),
      cls("h-full")
    )
}
