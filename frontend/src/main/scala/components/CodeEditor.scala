package components

import com.raquo.laminar.api.L._
import components.BasicSetup.{EditorState, EditorView}
import org.scalajs.dom
import org.scalajs.dom.window

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.timers.setTimeout

trait CodeMirrorConfig extends js.Object {
  val value: String
  val mode: String
  val theme: String
  val tabSize: Int
  val autofocus: Boolean
}

object CodeMirrorConfig {
  def apply(value: String, mode: String, theme: String, autofocus: Boolean = false): CodeMirrorConfig =
    js.Dynamic
      .literal(value = value, mode = mode, theme = theme, tabSize = 4, autofocus = autofocus)
      .asInstanceOf[CodeMirrorConfig]
}

@js.native
trait Doc extends js.Object {
  def setValue(value: String): Unit = js.native
}

@js.native
trait CodeMirror extends js.Object {
  def getValue(separator: String = ""): String = js.native

  def on(event: String, cb: js.Function): Unit = js.native

  def setSize(width: Int, height: Int): Unit = js.native

  def setSize(width: String, height: String): Unit = js.native

  def getInputField(): dom.html.Element = js.native

  def getDoc(): Doc = js.native
}

@js.native
@JSImport("codemirror", JSImport.Namespace)
object CodeMirror extends js.Object {
  def apply(any: Any, config: CodeMirrorConfig): CodeMirror = js.native
}

object CodeMirrorSyntax {
  implicit class CodeMirrorOps(private val self: CodeMirror) {
    def blur(): Unit = self.getInputField().blur()
  }
}

//@js.native
//@JSImport("codemirror/mode/clike/clike.js", JSImport.Default)
//object CodeMirrorScala extends js.Any

final case class CodeEditor(contentVar: Var[String] = Var(""), readOnly: Boolean = false) extends Component {
  var editor: BasicSetup.EditorView = _

  val submitBus                         = new EventBus[Unit]
  val $submissions: EventStream[String] = submitBus.events.mapTo(contentVar.now())

  println(s"KEMAP ${ViewNamespace.keymap}")

  def submit = submitBus.emit(())

  def body: Div = div(
    fontSize("16px"),
    onMountCallback { el =>
      val state = BasicSetup.EditorState.create(
        EditorStateConfig(
          doc = contentVar.now(),
          extensions = js.Array(
            StreamLanguage.define(CLikeMode),
            OneDarkTheme,
            EditorView.updateListener.of { viewUpdate =>
              contentVar.set(viewUpdate.state.doc.sliceString(0))
            },
            EditorState.readOnly.of(readOnly),
            ViewNamespace.keymap.of(
              js.Array(
                KeyBinding("Cmd-Enter", (_: Any) => { submit; true }),
                KeyBinding("Ctrl-Enter", (_: Any) => { submit; true }),
                KeyBinding("Alt-Enter", (_: Any) => { submit; true }),
                KeyBinding("Shift-Enter", (_: Any) => { submit; true }),
                KeyBinding("Enter", (_: Any) => { println("HELLO"); true })
              )
            )
          )
        )
      )

      editor = new BasicSetup.EditorView(
        EditorViewConfig(
          parent = el.thisNode.ref,
          state = state
        )
      )

      editor.focus()
    }
  )

}

//EditorView.updateListener.of(update => ...)

// CodeMirror 6

trait EditorViewConfig extends js.Object {
  val parent: dom.Element
  val state: EditorState
}

object EditorViewConfig {
  def apply(parent: dom.Element, state: EditorState): EditorViewConfig =
    js.Dynamic.literal(parent = parent, state = state).asInstanceOf[EditorViewConfig]
}

//@js.native
//trait EditorView extends js.Object {}

@js.native
trait Facet[A] extends js.Object {
  def of(input: A): Extension = js.native
}

@js.native
trait ViewUpdate extends js.Object {
  val state: EditorState
}

@js.native
trait KeyBinding extends js.Object {}

object KeyBinding {
  def apply(key: String, run: js.Function): KeyBinding =
    js.Dynamic.literal(key = key, run = run).asInstanceOf[KeyBinding]
}

@js.native
@JSImport("@codemirror/view", JSImport.Namespace)
object ViewNamespace extends js.Object {
  val keymap: Facet[js.Array[KeyBinding]] = js.native
}

@js.native
@JSImport("@codemirror/basic-setup", JSImport.Namespace)
object BasicSetup extends js.Object {

  @js.native
  class EditorView(config: EditorViewConfig) extends js.Object {
    val contentDOM: dom.html.Element = js.native

    def focus(): Unit = js.native
  }

  @js.native
  object EditorView extends js.Object {
    val updateListener: Facet[js.Function1[ViewUpdate, Any]] = js.native
  }

  @js.native
  object EditorState extends js.Object {
    def create(config: js.Object): EditorState = js.native

    val readOnly: Facet[Boolean] = js.native
  }

  val basicSetup: Extension = js.native
}

trait EditorStateConfig extends js.Object {
  val doc: String
  val extensions: List[Extension]
}

object EditorStateConfig {
  def apply(doc: String = "", extensions: js.Array[Extension]): EditorStateConfig =
    js.Dynamic.literal(doc = doc, extensions = extensions).asInstanceOf[EditorStateConfig]
}

@js.native
trait Extension extends js.Object {}

@js.native
trait Text extends js.Object {
  def sliceString(n: Int): String = js.native
}

@js.native
trait EditorState extends js.Object {
  val doc: Text

}

@js.native
@JSImport("@codemirror/legacy-modes/mode/clike", "scala")
object CLikeMode extends Extension {
//  def apply(config: js.Object): Extension = js.native
}

@js.native
@JSImport("@codemirror/stream-parser", "StreamLanguage")
object StreamLanguage extends js.Object {
  def define(value: Extension): Extension = js.native
}

@js.native
@JSImport("@codemirror/theme-one-dark", "oneDark")
object OneDarkTheme extends Extension {}

//@js.native
//@JSImport("@codemirror/basic-setup", "basicSetup")
//object BasicSetup extends Extension {}
