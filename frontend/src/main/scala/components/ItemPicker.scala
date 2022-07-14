package components

import com.raquo.laminar.api.L._

final case class ItemPicker(
  header: String,
  valueVar: Var[String],
  items: List[String]
) extends Component {
  val hasFocus = Var(false)

  def body =
    div(
      cls("p-4 border-b border-gray-800 hover:bg-gray-800"),
      cls.toggle("bg-gray-800") <-- hasFocus,
      div(
        cls("text-sm font-bold text-gray-400 tracking-widest mb-1"),
        header.toUpperCase
      ),
      items.map { item =>
        div(
          cls("flex items-center"),
          input(
            tpe("radio"),
            checked(item == items.head),
            onFocus.mapToStrict(true) --> hasFocus,
            onBlur.mapToStrict(false) --> hasFocus,
            idAttr(item),
            name(header),
            onClick.mapToStrict(item) --> valueVar
          ),
          label(
            item,
            cls("ml-2"),
            forId(item)
          )
        )
      }
    )
}
