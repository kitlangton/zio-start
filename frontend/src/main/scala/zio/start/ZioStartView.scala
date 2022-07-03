package zio.start

import animus.{SignalOps, SignalSeqOps, Transitions}
import components.Component
import com.raquo.laminar.api.L._

object ZioStartView extends Component {

  val groupVar    = Var("")
  val artifactVar = Var("")
  val packageVar  = Var("")
  val queryVar    = Var("")

  val searchMode = Var(false)

  val selectedDependencies =
    Var(Set.empty[Dependency])

  val $dependencies =
    searchMode.signal.combineWithFn(selectedDependencies.signal, queryVar.signal) { (isSearch, deps, query) =>
      if (isSearch)
        Dependency.all
          .filter(_.artifact.toLowerCase.contains(query.toLowerCase))
          .filterNot(deps.contains)
      else
        Dependency.all.filter(deps.contains)
    }

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
          List(
            zIndex(100),
            position.relative,
            cls("bg-gray-900"),
            div(
              SectionHeading("DEPENDENCIES"),
              Transitions.height(searchMode.signal.map(!_))
            ),
            SearchField(queryVar, searchMode).body,
            HorizontalSeparator().body.amend(
              position.absolute
//              top("1px")
//              Transitions.opacity(searchMode.signal.map(!_))
            ),
            children <-- $dependencies.splitTransition(identity) { (_, dep, _, t) =>
              div(
                DependencyView(
                  dep,
                  searchMode.signal,
                  isSearching =>
                    if (isSearching) {
                      selectedDependencies.update(_ + dep)
                      searchMode.set(false)
                    } else {
                      selectedDependencies.update(_ - dep)
                    }
                ),
                t.height,
                t.opacity
              )
            },
            HorizontalSeparator().body.amend(
              Transitions.opacity($dependencies.signal.map(_.nonEmpty))
            ),
//            Dependency.all.map(DependencyView(_).body): Mod[HtmlElement],
            left("-1px"),
            cls("border-x")
          )
        }
//        Column {
//          Section("ACTIONS")
//        }
      ),
      div(
        zIndex(50),
        position.absolute,
        cls("w-screen h-screen inset-0"),
        background("#14110E"),
        opacity <-- searchMode.signal.map(if (_) 0.5 else 0.0).spring,
        onClick --> { _ => searchMode.set(false) },
        pointerEvents <-- searchMode.signal.map(if (_) "auto" else "none")
      )
    )
}

final case class Column(content: Mod[HtmlElement]) extends Component {
  def body =
    div(
      cls("border-r border-gray-800 h-full"),
      position.relative,
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
  queryVar: Var[String],
  searchVar: Var[Boolean]
) extends Component {
  val $notSearching = searchVar.signal.map(!_)

  def body =
    div(
      windowEvents.onKeyDown --> { key =>
        key.key match {
          case "d" if key.ctrlKey =>
            searchVar.update(!_)
          case _ =>

        }
      },
      onClick --> { _ =>
        searchVar.update(!_)
      },
      cls("p-4 flex items-center justify-between"),
      cls("hover:bg-gray-800 cursor-pointer"),
      div(
        cls("flex items-center w-full"),
        div(
          div(
            Icons.plus,
            Transitions.height($notSearching),
            Transitions.opacity($notSearching)
          ),
          div(
            Icons.magnifier,
            Transitions.height(searchVar.signal),
            Transitions.opacity(searchVar.signal)
          )
        ),
        div(
          cls("w-full"),
          div(
            div(
              cls("flex justify-between items-center"),
              div(
                cls("pl-3 font-bold text-gray-400 tracking-wider whitespace-nowrap w-full"),
                div("ADD DEPENDENCY")
              ),
              div(
                "CTRL-D",
                cls(
                  "text-gray-400 text-xs tracking-wider ml-3 self-end whitespace-nowrap",
                  "p-1 px-2 bg-gray-800 rounded"
                )
              )
            ),
            Transitions.height($notSearching),
            Transitions.opacity($notSearching)
//            hidden <-- searchVar
          ),
          div(
            input(
              textTransform.uppercase,
              focus <-- searchVar.signal.changes,
              cls("pl-3 font-bold tracking-wider"),
              background("none"),
              outline("none"),
              placeholder("SEARCH"),
              controlled(
                value <-- queryVar,
                onInput.mapToValue --> queryVar
              )
            ),
            hidden <-- $notSearching,
            Transitions.height(searchVar.signal),
            Transitions.opacity(searchVar.signal)
          )
        )
      )
    )
}

final case class DependencyView(
  dependency: Dependency,
  isSearching: Signal[Boolean],
  handleClick: (Boolean) => Unit
) extends Component {

  val isHovered = Var(false)

  val $isHovered =
    EventStream
      .merge(
        isSearching.changes.mapTo(false),
        isHovered.signal.changes
      )
      .startWith(false)

  val $isHoveredAndSearching    = $isHovered.combineWithFn(isSearching)(_ && _)
  val $isHoveredAndNotSearching = $isHovered.combineWithFn(isSearching)(_ && !_)

  def body =
    div(
      HorizontalSeparator(),
      onMouseEnter.mapToStrict(true) --> isHovered,
      onMouseLeave.mapToStrict(false) --> isHovered,
      composeEvents(onClick)(_.sample(isSearching)) --> { bool => handleClick(bool) },
      div(
        cls("p-4 cursor-pointer"),
        cls.toggle("subtle-red") <-- $isHoveredAndNotSearching,
        cls.toggle("subtle-green") <-- $isHoveredAndSearching,
        div(
          cls("flex items-center"),
          div(
            opacity <-- $isHovered.map(if (_) 0.0 else 1.0).spring,
            div(
              cls("flex items-center"),
              opacity(0.7),
              Icons.cylinder,
              div(cls("w-2"), nbsp)
            ),
            Transitions.width($isHovered.map(!_))
          ),
          div(
            opacity <-- $isHovered.map(if (_) 1.0 else 0.0).spring,
            div(
              cls("flex items-center"),
              Icons.remove,
              div(cls("w-2"), nbsp)
            ),
            Transitions.width($isHoveredAndNotSearching)
          ),
          div(
            opacity <-- $isHovered.map(if (_) 1.0 else 0.0).spring,
            div(
              cls("flex items-center"),
              Icons.add,
              div(cls("w-2"), nbsp)
            ),
            Transitions.width($isHoveredAndSearching)
          ),
          div(
            cls("font-bold text-gray-300 tracking-wider"),
            dependency.artifact.toUpperCase
          )
        ),
        div(
          cls("text-gray-400 mt-2"),
          dependency.description
        )
      )
    )
}
