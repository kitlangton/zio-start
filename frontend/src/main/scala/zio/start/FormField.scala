package zio.start

import com.raquo.laminar.api.L._
import components.Component

final case class FormField(
  name: String,
  valueVar: Var[String],
  default: Signal[String],
  mapping: String => String = identity,
  autofocused: Boolean = false,
  handleTab: Option[() => Unit] = None
) extends Component {

  val isFocused = Var(false)

  val focusBus = new EventBus[Boolean]

  def body =
    div(
      cls("p-4 border-b border-gray-800 hover:bg-gray-800"),
      onClick --> { _ =>
        focusBus.emit(true)
      },
      cls.toggle("bg-gray-800") <-- isFocused.signal,
      div(
        cls("text-sm font-bold text-gray-400 tracking-widest mb-1"),
        name.toUpperCase
      ),
      input(
        autoFocus(autofocused),
        inContext { el =>
          onFocus --> { _ =>
            // select all text on focus
            el.ref.select()
          }
        },
        focus <-- focusBus,
        cls("w-full"),
        outline("none"),
        background("none"),
        controlled(
          value <-- valueVar,
          onInput.mapToValue.map(mapping) --> valueVar
        ),
        onFocus.mapToStrict(true) --> isFocused,
        onBlur.mapToStrict(false) --> isFocused,
        onKeyDown --> { e =>
          e.key match {
            case "Tab" if !e.shiftKey =>
              handleTab.foreach(_())
            case _ =>
          }
        },
        placeholder <-- default
      )
    )
}
