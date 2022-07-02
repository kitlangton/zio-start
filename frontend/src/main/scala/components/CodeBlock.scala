package components

import com.raquo.laminar.api.L._

final case class CodeBlock(code: String, result: Option[String] = None, marginTop: Int = 4) extends Component {
  def body: Div =
    div(
      cls(s"text-gray-200 overflow-hidden font-mono mt-$marginTop"),
      div(
        div(
          cls("relative"),
          //          div(
          //            cls(
          //              "text-gray-400 absolute right-4 cursor-pointer hover:text-yellow-400 transition-colors " +
          //                "duration-500 hover:duration-100"
          //            ),
          //            Icons.lightBulbFill
          //          ),
          cls(s"p-4 overflow-x-auto bg-gray-700 rounded-t"),
          cls.toggle("rounded-b")(result.isEmpty),
          HighlightedCode(code)
        ),
        div(
          cls(s"p-4 relative text-gray-400 rounded-b"),
          cls.toggle(s"hidden")(result.isEmpty),
          color("#B2B8C2"),
          background("#243349"),
          result.map(HighlightedCode)
        )
      )
    )

}
