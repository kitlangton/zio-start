package zio.start

import animus.{SignalOps, Transitions}
import components.Component
import com.raquo.laminar.api.L._

object ZioStartView extends Component {

  val groupVar    = Var("")
  val artifactVar = Var("")
  val packageVar  = Var("")
  val queryVar    = Var("")

  val $groupDefault =
    groupVar.signal.map { str =>
      if (str.isEmpty) "io.github.kitlangton"
      else str
    }

  val $artifactDefault =
    artifactVar.signal.map { str =>
      if (str.isEmpty) "zio-start".replace("-", ".")
      else str
    }

  val $packageDefault: Signal[String] =
    $groupDefault.combineWithFn($artifactDefault) { (group, artifact) =>
      s"$group.$artifact"
    }

  def body =
    div(
      cls("bg-gray-900 text-gray-100 h-screen font-mono"),
      div(
        cls("flex h-screen"),
        div(
          cls("flex flex-col justify-between items-center"),
          cls("border-r border-gray-800 h-full"),
          div(
            div(
              cls("px-6 py-6"),
              div(
                cls(
                  "whitespace-nowrap font-mono text-xl font-bold border border-red-900 text-red-600 p-2 px-3 rounded"
                ),
                s"ZIO✦START".toList.map { char =>
                  if (char == '✦')
                    div(cls("text-red-900"), char.toString)
                  else
                    div(char.toString)
                }
              )
            )
          ),
          div(
            cls("flex flex-col justify-between items-center py-8"),
            a(
              cls("opacity-75 hover:opacity-100"),
              Icons.github,
              href("https://github.com/kitlangton/zio-start"),
              target("_blank")
            ),
            div(cls("h-8")),
            a(
              cls("opacity-75 hover:opacity-100"),
              Icons.twitter,
              href("https://twitter.com/kitlangton"),
              target("_blank")
            )
          )
        ),
        Column {
          div(
            SectionHeading("INSTRUCTIONS"),
            div(
              div(
                cls("text-normal text-gray-400 p-4 mb-2"),
                div(
                  s"Create a${nbsp}",
                  span("ZIO", cls("text-red-600 font-bold tracking-wider")),
                  s"${nbsp}project with the dependencies you want."
                ),
                div(cls("mt-4"), "Download your project directory and get started.")
              ),
              HorizontalSeparator()
            ),
            SectionHeading("PROJECT INFO"),
            FormField( //
              "group",
              groupVar,
              Val("io.github.kitlangton"),
              _.replace(" ", ".")
            ),
            FormField( //
              "artifact",
              artifactVar,
              Val("zio-start"),
              _.replace(" ", "-")
            ),
            FormField( //
              "package",
              packageVar,
              $packageDefault,
              _.replace(" ", ".")
            )
          )
        },
        Column {
          div(
            SectionHeading("DEPENDENCIES"),
            SearchField(queryVar),
            HorizontalSeparator(),
            DependencyView("zio-json", "A performant library for JSON Encoding and Decoding."),
            DependencyView("zio-http", "A supercharged, ergonomic library for building HTTP servers."),
            DependencyView("zio-kafka", "A Kafka client for ZIO.")
          )
        }
//        Column {
//          Section("ACTIONS")
//        }
      )
    )
}

final case class Column(content: Mod[HtmlElement]) extends Component {
  def body =
    div(
      cls("border-r border-gray-800 h-full"),
      width("420px"),
      content
    )
}

final case class SectionHeading(name: String) extends Component {
  def body =
    div(
      cls("w-full"),
      div(
        cls("text-sm p-2 px-3 tracking-wider font-medium text-gray-300"),
        name
      ),
      HorizontalSeparator()
    )
}

final case class HorizontalSeparator() extends Component {
  def body: HtmlElement =
    div(
      cls("w-full h-px bg-gray-800")
    )
}

final case class SearchField(
  queryVar: Var[String]
) extends Component {
  def body =
    div(
      cls("p-4 flex items-center justify-between"),
      cls("hover:bg-gray-800 cursor-pointer"),
      div(
        cls("flex items-center"),
        Icons.plus,
        div(
          cls("pl-3 font-bold text-gray-400 tracking-wider"),
          "ADD DEPENDENCY"
        )
      ),
      div(
        "CTRL-D",
        cls(
          "text-gray-400 text-xs tracking-wider ml-3 self-end",
          "p-1 px-2 bg-gray-800 rounded"
        )
      )
    )
}

final case class DependencyView(
  name: String,
  description: String
) extends Component {

  val isHovered = Var(false)

  def body =
    div(
      onMouseEnter.mapToStrict(true) --> isHovered,
      onMouseLeave.mapToStrict(false) --> isHovered,
      div(
        cls("p-4 cursor-pointer"),
        cls.toggle("subtle-red") <-- isHovered,
        div(
          cls("flex items-center"),
          div(
            opacity <-- isHovered.signal.map(if (_) 0.0 else 1.0).spring,
            div(
              cls("flex items-center"),
              opacity(0.7),
              Icons.cylinder,
              div(cls("w-2"), nbsp)
            ),
            Transitions.width(isHovered.signal.map(!_))
          ),
          div(
            opacity <-- isHovered.signal.map(if (_) 1.0 else 0.0).spring,
            div(
              cls("flex items-center"),
              Icons.remove,
              div(cls("w-2"), nbsp)
            ),
            Transitions.width(isHovered.signal)
          ),
          div(
            cls("font-bold text-gray-300 tracking-wider"),
            name.toUpperCase
          )
        ),
        div(
          cls("text-gray-400 mt-2"),
          description
        )
      ),
      HorizontalSeparator()
    )
}
