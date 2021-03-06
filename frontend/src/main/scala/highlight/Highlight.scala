package highlight

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("highlight.js", JSImport.Default)
object Highlight extends Highlight

@js.native
trait Highlight extends js.Object {
  def initHighlighting(): Unit = js.native

  def highlight(languageName: String, code: String): HighlightedText = js.native
  def highlightAuto(code: String): HighlightedText                   = js.native
}

@js.native
trait HighlightedText extends js.Object {
  val value: String
}
