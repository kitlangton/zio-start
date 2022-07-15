package zio.start

import animus.{SignalOps, SignalSeqOps, Transitions}
import com.raquo.laminar.api.L._
import components.{CodeBlock, Component, ItemPicker}
import zip.{FileGenerator, FileStructure}

final case class Rendered(
  isFolder: Boolean,
  indent: Int,
  name: String,
  contents: String,
  fileStructure: FileStructure
)

final case class FilePreview(fileStructure: FileStructure) extends Component {

  def allFiles(fileStructure: FileStructure): List[FileStructure.Folder] =
    fileStructure match {
      case folder @ FileStructure.Folder(name, contents) =>
        folder :: contents.collect { case folder: FileStructure.Folder => allFiles(folder) }.flatten
      case FileStructure.File(name, contents) => Nil
    }

  val openedVar: Var[Set[FileStructure]] = Var(allFiles(fileStructure).toSet)
  val selectedFileVar: Var[FileStructure.File] =
    Var(
      fileStructure match {
        case FileStructure.Folder(name, contents) =>
          contents.collectFirst { case file @ FileStructure.File(name, _) => file }.get
        case file @ FileStructure.File(name, contents) => file
      }
    )

  def renderFile(fileStructure: FileStructure, indent: Int, opened: Set[FileStructure]): List[Rendered] =
    fileStructure match {
      case FileStructure.Folder(name, contents) =>
        val isOpened = opened.contains(fileStructure)
        val children =
          if (isOpened) contents.flatMap(renderFile(_, indent + 1, opened))
          else Nil
        Rendered(
          isFolder = true,
          indent = indent,
          name = name,
          contents = "",
          fileStructure = fileStructure
        ) +: children
      case FileStructure.File(name, contents) =>
        Rendered(
          isFolder = false,
          indent = indent,
          name = name,
          contents = contents,
          fileStructure = fileStructure
        ) :: Nil
    }

  val $rendered = openedVar.signal.map { opened =>
    renderFile(fileStructure, 0, opened)
  }

  def body =
    div(
      cls("grid grid-cols-3 h-full"),
      Column {
        div(
          SectionHeading(
            "Generated"
          ).amend(position("sticky"), zIndex(999), top("0"), cls("bg-gray-900 shadow")),
          children <-- $rendered.splitTransition(_.fileStructure) { (_, r, rendered, t) =>
            val view =
              if (r.isFolder)
                div(
                  cls("flex items-center fill-gray-500"),
                  div(
                    cls("fill-gray-600 transitions-rotate duration-200"),
                    cls.toggle("rotate-90") <-- openedVar.signal.map(_.contains(r.fileStructure)),
                    Icons.chevronRight
                  ),
                  Icons.folder,
                  div(
                    cls("ml-2"),
                    s"${r.name}"
                  )
                )
              else
                div(
                  cls("flex items-center fill-gray-500 pl-5"),
                  Icons.document,
                  div(
                    cls("ml-2"),
                    s"${r.name}"
                  )
                )

            div(
              div(
                cls("border-b border-gray-800 p-2 hover:bg-gray-800 cursor-pointer"),
                cls("select-none"),
                cls.toggle("bg-red-900 hover:bg-red-800") <-- selectedFileVar.signal.map(_ == r.fileStructure),
                div(
                  paddingLeft(s"${r.indent * 8}px"),
                  view
                ),
                onClick --> { _ =>
                  r.fileStructure match {
                    case folder: FileStructure.Folder =>
                      openedVar.update { opened =>
                        if (opened.contains(folder)) opened - folder
                        else opened + folder
                      }
                    case file: FileStructure.File =>
                      selectedFileVar.set(file)
                  }
                }
              ),
              t.height,
              position.relative,
              zIndex(100 - r.indent),
              top <-- t.$isActive.map {
                case true  => 0.0
                case false => -40.0
              }.spring.px,
              t.opacity
            )
          }
        )
      }.amend(width("100%"), overflowY.scroll),
      Column {
        child <-- selectedFileVar.signal.map { file =>
          div(
            SectionHeading(file.name),
            CodeBlock(file.contents, lang = file.language)
          )
        }
      }.amend(width("100%"), cls("col-span-2"))
    )

//  def renderFile(fileStructure: FileStructure) =
//    fileStructure match {
//      case FileStructure.Folder(name, contents) =>
//        div(name)
//      case FileStructure.File(name, contents) =>
//        div(name)
//
//    }
}

object ZioStartView extends Component {

  val groupVar        = Var("")
  val artifactVar     = Var("")
  val packageVar      = Var("")
  val descriptionVar  = Var("")
  val queryVar        = Var("")
  val scalaVersionVar = Var("")
  val searchIndex     = Var(0)

  val scalaVersionOptions = List("3.1.3", "2.13.8", "2.12.16")

  val searchMode     = Var(false)
  val generatedFile  = Var(Option.empty[FileStructure])
  val $showGenerated = generatedFile.signal.map(_.isDefined)

  val selectedDependencies =
    Var(Set.empty[Dependency])

  val $dependencies: Signal[List[Dependency]] =
    searchMode.signal.combineWithFn(selectedDependencies.signal, queryVar.signal) { (isSearch, deps, query) =>
      if (isSearch)
        Dependency.all
          .filter(_.artifact.toLowerCase.contains(query.toLowerCase))
//          .filterNot(deps.contains)
      else
        Dependency.all.filter(deps.contains)
    }

  val $searchIndex: Signal[Int] =
    searchIndex.signal

  val $groupDefault: Signal[String] =
    groupVar.signal.map { str =>
      if (str.isEmpty) "com.kitlangton"
      else str
    }

  val $artifactDefault: Signal[String] =
    artifactVar.signal.map { str =>
      if (str.isEmpty) "zio-start".replace("-", ".")
      else str
    }

  val $packageDefault: Signal[String] =
    $groupDefault.combineWithFn($artifactDefault) { (group, artifact) =>
      s"$group.${artifact.replace("-", ".")}"
    }

  val $descriptionDefault: Signal[String] =
    descriptionVar.signal.map { str =>
      if (str.isEmpty) "An incredible project."
      else str
    }

  def body =
    div(
      div(
        cls("sm:hidden bg-gray-900 h-screen"),
        cls("grid grid-rows-5"),
        div(
          cls("flex items-center justify-center h-full row-span-3 shadow-md"),
          ZioStartTitle
        ),
        div(
          cls("text-slate-500 text-center font-mono px-12 font-bold flex items-center justify-center h-full"),
          cls("flex-col border-gray-800 stroke-slate-600 border-t shadow-md"),
          Icons.desktop,
          div(
            div("TRY AGAIN ON"),
            div("A LARGER DEVICE"),
            cls("mt-2")
          )
        ),
        div(
          cls("flex items-center justify-center border-gray-800 border-t shadow-lg"),
          a(
            cls("opacity-75 hover:opacity-100"),
            Icons.github,
            href("https://github.com/kitlangton/zio-start"),
            target("_blank")
          ),
          div(cls("w-8")),
          a(
            cls("opacity-75 hover:opacity-100"),
            Icons.twitter,
            href("https://twitter.com/kitlangton"),
            target("_blank")
          )
        )
      ),
      div(
        cls("hidden sm:block"),
        view
      )
    )

  def view =
    div(
      $dependencies.changes.mapToStrict(0) --> searchIndex,
      windowEvents.onKeyDown.withCurrentValueOf($dependencies) --> { value =>
        val (e, deps)   = value
        val isSearching = searchMode.now()
        e.key match {
          case "ArrowUp" if isSearching =>
            searchIndex.update { idx =>
              if (idx > 0) idx - 1
              else idx
            }
          case "ArrowDown" if isSearching =>
            searchIndex.update { idx =>
              if (idx < deps.size - 1) idx + 1
              else idx
            }
          case "Enter" if isSearching =>
            val dep = deps(searchIndex.now())
            selectedDependencies.update { deps =>
              if (deps.contains(dep)) deps - dep
              else deps + dep
            }
          case "d" if e.ctrlKey =>
            if (!isSearching)
              searchIndex.set(0)
            searchMode.update(!_)
          case "Escape" if isSearching =>
            e.preventDefault()
            e.stopPropagation()
            searchMode.set(false)
          case _ =>
        }
      },
      searchMode.signal --> { m =>
        if (!m) queryVar.set("")
      },
      cls("bg-gray-900 text-gray-100 h-screen font-mono"),
      div(
        cls("flex h-screen"),
        div(
          cls("flex flex-col justify-between items-center"),
          cls("border-r border-gray-800 h-full"),
          ZioStartTitle,
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
                div(cls("mt-4"), "When you're finished, download your project folder and get started."),
                div(cls("mt-4"), "Have fun!")
              ),
              HorizontalSeparator()
            ),
            SectionHeading("PROJECT INFO"),
            ItemPicker(
              "scala version",
              scalaVersionVar,
              scalaVersionOptions
            ),
            FormField( //
              "group",
              groupVar,
              Val("com.kitlangton"),
              _.replace(" ", "."),
              true
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
            ),
            FormField(
              "description",
              descriptionVar,
              Val("An incredible project."),
              handleTab = Some(() => searchMode.set(true))
            )
          )
        },
        Column {
          List(
            zIndex(100),
            position.relative,
            overflow.hidden,
            cls("bg-gray-900"),
            div(
              div(
                SectionHeading("DEPENDENCIES"),
                height("37px"),
                Transitions.height(searchMode.signal.map(!_)),
                Transitions.opacity(searchMode.signal.map(!_))
              ),
              SearchField(queryVar, searchMode).body,
              HorizontalSeparator().body.amend(position.absolute),
              cls("sticky top-0 relative"),
              zIndex(110)
            ),
            div(
              overflowY.scroll,
              children <-- $dependencies.map(_.zipWithIndex).splitTransition(_._1) { case (_, (dep, _), id, t) =>
                val $idx = id.map(_._2)
                div(
                  DependencyView(
                    dependency = dep,
                    isSearching = searchMode.signal,
                    isSelected = selectedDependencies.signal.map(_.contains(dep)),
                    isHighlighted = $searchIndex.combineWithFn($idx, searchMode)(_ == _ && _),
                    handleClick = isSearching =>
                      if (isSearching) {
                        selectedDependencies.update { deps =>
                          if (deps.contains(dep)) deps - dep
                          else deps + dep
                        }
                        //                      searchMode.set(false)
                      } else {
                        selectedDependencies.update(_ - dep)
                      }
                  ),
                  t.height,
                  t.opacity
                )
              }
            ),
            HorizontalSeparator().body.amend(
              Transitions.opacity($dependencies.signal.map(_.nonEmpty))
            ),
            position.relative,
            div(
              flexGrow(1)
            ),
            div(
              cls("h-16")
            ),
            div(
              cls("w-full"),
              div(
                onClick --> { _ =>
                  searchMode.set(false)
                  queryVar.set("")
                  searchIndex.set(0)
                },
                cls(
                  "bottom-0 sticky z-index-100 bg-gray-900 w-full",
                  "cursor-pointer hover:bg-gray-800"
                ),
                HorizontalSeparator(),
                div(
                  cls(
                    "p-4 font-bold text-gray-400 tracking-wider",
                    "cursor-pointer text-center"
                  ),
                  "DONE"
                ),
                Transitions.opacity(searchMode.signal)
              ),
              position.absolute,
              bottom <-- searchMode.signal.map {
                if (_) 0.0
                else -40.0
              }.spring.px
            ),
            left("-1px"),
            cls("border-x flex flex-col")
          )
        },
        Column {
          div(
            SectionHeading("ACTIONS"),
            div(
              tabIndex(0),
              cls("p-6 font-bold text-gray-400 subtle-blue cursor-pointer"),
              cls("fill-gray-500 hover:fill-orange-400"),
              div(
                cls("flex items-center"),
                Icons.folderDownload,
                div(
                  cls("pl-3"),
                  "DOWNLOAD PROJECT"
                )
              ),
              div(
                "",
                cls("text-xs text-gray-500")
              ),
              cls("focus:bg-green-900 focus:text-green-300 focus:fill-green-400"),
              outline("none"),
              inContext { el =>
                composeEvents(onKeyDown)(_.withCurrentValueOf($packageDefault)) --> { value =>
                  val (e, defaultPackage) = value
                  if (e.key == "Enter") {
                    downloadProject(defaultPackage)
                    el.ref.blur()
                  }
                }
              },
              composeEvents(onClick)(_.sample($packageDefault)) --> { defaultPackage =>
                downloadProject(defaultPackage)
              }
            ),
            HorizontalSeparator(),
            div(
              cls("p-6 font-bold text-gray-400 subtle-blue cursor-pointer"),
              cls("fill-gray-500 hover:fill-orange-400"),
              div(
                cls("flex items-center"),
                Icons.eye,
                div(
                  cls("pl-3"),
                  "PREVIEW GENERATED CODE"
                )
              ),
              composeEvents(onClick)(_.sample($packageDefault)) --> { defaultPackage =>
                val fs = generateFileStructure(defaultPackage)
                generatedFile.set(Some(fs))
              }
            ),
            HorizontalSeparator(),
            div(
              cls("p-6 font-bold text-gray-600 hover:bg-gray-800"),
              "COPY LINK",
              div(
                "COMING SOON",
                cls("text-xs text-gray-700")
              )
            ),
            HorizontalSeparator()
          )
        }
      ),
      div(
        zIndex(50),
        position.absolute,
        cls("w-screen h-screen inset-0"),
        background("#14110E"),
        opacity <-- searchMode.signal.map(if (_) 0.5 else 0.0).spring,
        onClick --> { _ => searchMode.set(false) },
        pointerEvents <-- searchMode.signal.map(if (_) "auto" else "none")
      ),
      div(
        zIndex(160),
        position.absolute,
        cls("inset-16 bg-gray-900 border border-gray-800 shadow-lg rounded"),
        child <-- generatedFile.signal.changes.collect { case Some(f) => f }.map { gen =>
          FilePreview(
            gen
          )
        },
        opacity <-- $showGenerated.map(if (_) 1.0 else 0.0).spring,
        pointerEvents <-- $showGenerated.map(if (_) "auto" else "none"),
        windowEvents.onKeyDown --> { e =>
          if (e.key == "Escape") {
            generatedFile.set(None)
          }
        }
      ),
      div(
        zIndex(150),
        position.absolute,
        cls("w-screen h-screen inset-0"),
        background("#14110E"),
        onClick --> { _ => generatedFile.set(None) },
        opacity <-- $showGenerated.map(if (_) 0.6 else 0.0).spring,
        pointerEvents <-- $showGenerated.map(if (_) "auto" else "none")
      )
    )

  private def ZioStartTitle =
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

  def notBlankOrElse(s: String, default: String): String =
    if (s.isEmpty) default
    else s

  private def generateFileStructure(defaultPackage: String) = {
    val scalaVersion = notBlankOrElse(scalaVersionVar.now(), scalaVersionOptions.head)
    val group        = notBlankOrElse(groupVar.now(), "com.kitlangton")
    val artifact     = notBlankOrElse(artifactVar.now(), "zio-start")
    val packageName  = notBlankOrElse(packageVar.now(), defaultPackage)
    val description  = notBlankOrElse(descriptionVar.now(), "An incredible project.")
    val selected     = selectedDependencies.now()

    FileGenerator.generateFileStructure(
      scalaVersion,
      group,
      artifact,
      packageName,
      description,
      selected.toList
        .flatMap(d => d :: d.included)
        .distinct
        .sortBy(d => (d.group, d.artifact))
    )
  }

  private def downloadProject(defaultPackage: String) = {
    val artifact      = notBlankOrElse(artifactVar.now(), "zio-start")
    val fileStructure = generateFileStructure(defaultPackage)

    FileGenerator.generateZip(artifact, fileStructure)
  }
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
      cls("transition-colors bg-gray-900"),
      cls.toggle("subtle-highlight") <-- searchVar,
      onClick --> { _ =>
        searchVar.set(true)
      },
      cls("p-4 flex items-center justify-between"),
      cls("hover:bg-gray-800 cursor-pointer"),
      div(
        cls("flex items-center w-full"),
        div(
          div(
            cls("fill-gray-500"),
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
                cls("pl-2 font-bold text-gray-400 tracking-wider whitespace-nowrap w-full"),
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
            div(
              cls("flex relative"),
              input(
                textTransform.uppercase,
                focus <-- searchVar.signal.changes.delay(100),
                cls("pl-3 font-bold tracking-wider"),
                background("none"),
                outline("none"),
                placeholder("SEARCH"),
                onKeyDown --> { e =>
                  if (e.key == "ArrowDown" || e.key == "ArrowUp") {
                    e.preventDefault()
                  }
                  if (e.key == "Tab") {
                    searchVar.set(false)
                  }
                },
                controlled(
                  value <-- queryVar,
                  onInput.mapToValue --> queryVar
                )
              )
//              div(
//                position.absolute,
//                right("0px"),
//                "ESC",
//                cls(
//                  "text-gray-400 text-xs tracking-wider ml-3 self-end whitespace-nowrap flex-end",
//                  "p-1 px-2 bg-gray-800 rounded align-end self-end"
//                )
//              )
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
  isSelected: Signal[Boolean],
  isHighlighted: Signal[Boolean],
  handleClick: Boolean => Unit
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

  val $showCheckmark =
    isSelected.combineWithFn(isSearching)(_ && _)

  val $showAdd =
    isSelected.combineWithFn(isSearching, isHovered) { (selected, searching, hovered) =>
      !selected && searching && hovered
    }

  val $showCylinderIcon =
    isSelected.combineWithFn(isSearching, $isHovered) { (selected, searching, hovered) =>
      (!searching && !hovered) || (searching && !selected && !hovered)
    }

  def body =
    div(
      HorizontalSeparator(),
      onMouseEnter.mapToStrict(true) --> isHovered,
      onMouseLeave.mapToStrict(false) --> isHovered,
      composeEvents(onClick)(_.sample(isSearching)) --> { bool => handleClick(bool) },
//      cls("w-full bg-red-500 hover:bg-red-700 cursor-pointer"),
      div(
        width("100%"),
        cls("flex items-center"),
        flexGrow(1),
        position.relative,
        div(
          cls("inset-y-0 absolute w-0.5"),
          cls <-- isHighlighted.map(if (_) "bg-green-800" else "")
        ),
        div(
          cls("p-4 cursor-pointer w-full"),
          cls.toggle("subtle-red") <-- $isHoveredAndNotSearching,
          cls.toggle("subtle-green") <-- $isHoveredAndSearching,
          div(
            cls("flex items-center"),
            div(
              cls("w-full"),
              div(
                cls("flex items-center font-medium text-sm text-green-600 mr-2"),
                s"ADDED${nbsp}"
              ),
              Transitions.width($showCheckmark),
              Transitions.opacity($showCheckmark)
            ),
            div(
              div(
                cls("flex items-center w-6"),
                div(
                  div(
                    cls("flex items-center fill-green-600"),
                    Icons.add,
                    div(cls("w-2"), nbsp)
                  ),
                  Transitions.width($showAdd),
                  Transitions.opacity($showAdd)
                ),
                div(
                  div(
                    cls("flex items-center"),
                    opacity(0.7),
                    Icons.cylinder,
                    div(cls("w-2"), nbsp)
                  ),
                  Transitions.width($showCylinderIcon),
                  Transitions.opacity($showCylinderIcon)
                ),
                div(
                  opacity <-- $isHovered.map(if (_) 1.0 else 0.0).spring,
                  div(
                    cls("flex items-center"),
                    Icons.remove,
                    div(cls("w-2"), nbsp)
                  ),
                  Transitions.width($isHoveredAndNotSearching)
                )
              ),
              Transitions.width($showCheckmark.map(!_))
            ),
            div(
              cls("flex justify-between items-center w-full"),
              div(
                cls("font-bold text-gray-300 tracking-wider"),
                dependency.name.toUpperCase
              ),
              a(
                cls("stroke-gray-700 hover:stroke-blue-500"),
                width("20px"),
                onMouseEnter.mapToStrict(false) --> isHovered,
                onMouseLeave.mapToStrict(true) --> isHovered,
                Icons.externalLink,
                href(dependency.url),
                target("_blank"),
                onClick --> { e => e.stopPropagation() }
              )
            )
          ),
          div(
            cls("text-gray-400 mt-2"),
            dependency.description
          )
        )
      )
    )
}
