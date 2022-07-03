package zip

import zio.start.Dependency

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobal, JSImport}

@js.native
@JSGlobal("JSZip")
class JSZip() extends js.Object {
  def file(name: String, content: String): JSZip         = js.native
  def folder(name: String): JSZip                        = js.native
  def generateAsync(options: js.Object): js.Promise[Any] = js.native
}

@js.native
@JSImport("file-saver", JSImport.Namespace)
object FileSaver extends js.Object {
  def saveAs(file: Any, name: String): Unit = js.native
}

sealed trait FileStructure extends Product with Serializable {
  def name: String
}

object FileStructure {
  final case class Folder(name: String, contents: List[FileStructure]) extends FileStructure {
    def add(path: List[String], files: List[FileStructure]): Folder =
      path match {
        case head :: tail =>
          val matchIndex = contents.indexWhere(_.name == head)
          if (matchIndex == -1) {
            copy(contents = contents :+ Folder(head, Nil).add(tail, files))
          } else {
            val newContents =
              contents.updated(matchIndex, contents(matchIndex).asInstanceOf[Folder].add(tail, files))
            copy(contents = newContents)
          }
        case Nil =>
          copy(contents = contents ++ files)
      }

    def add(path: String*)(files: FileStructure*): Folder = add(path.toList, files.toList)
  }

  final case class File(name: String, contents: String) extends FileStructure

  def folderPath(path: String, paths: String*)(contents: FileStructure*): Folder =
    Folder(path, List.empty).add(paths.toList, contents.toList)

}

object FileGenerator {

  def generateZip(name: String, fileStructure: FileStructure): Unit = {
    val zipFile = new zip.JSZip()

    def traverse(zip: JSZip, fileStructure: FileStructure): Unit =
      fileStructure match {
        case FileStructure.Folder(name, contents) =>
          val folder = zip.folder(name)
          contents.foreach(traverse(folder, _))
        case FileStructure.File(name, contents) =>
          zip.file(name, contents)
      }

    traverse(zipFile, fileStructure)

    zipFile.generateAsync(js.Dynamic.literal("type" -> "blob")).toFuture.foreach { result =>
      FileSaver.saveAs(result, s"zio-start-$name.zip")
    }
  }

  def generateFileStructure(
    group: String,
    artifact: String,
    packageName: String,
    dependencies: List[Dependency]
  ): FileStructure = {

    val buildSbt = generateBuildSbt(group, artifact, dependencies)

    val readmeFile =
      FileStructure.File(
        "README.md",
        s"""
           |# $artifact
           |Good luck with your incredible project!
           |
           |Generated with [zio-start](http://zio-start.surge.sh)
           |""".stripMargin
      )

    val mainFile = FileStructure.File(
      "Main.scala",
      s"""
package $packageName

import zio._

object Main extends ZIOAppDefault {

  val run = Console.printLine("Hello, $artifact!")

}
           """.trim
    )

    val specFile = FileStructure.File(
      "MainSpec.scala",
      s"""
package $packageName

import zio.test._
import zio._

object MainSpec extends ZIOSpecDefault {

  def spec = 
    suite("MainSpec")(
      test("it works!") {
        val result = 10
        assertTrue(result == 10)
      }
    )
    
}
           """.trim
    )

    val sourcePath =
      "scala" :: packageName.split("\\.").toList

    FileStructure.Folder(
      artifact,
      List(
        FileStructure.folderPath("modules", "core")(
          FileStructure.Folder(
            "src",
            List(
              FileStructure.folderPath("main", sourcePath: _*)(mainFile),
              FileStructure.folderPath("test", sourcePath: _*)(specFile)
            )
          )
        ),
        readmeFile,
        buildSbt,
        scalafmtFile
      )
    )

  }

  def indent(string: String, indent: Int): String =
    string.split("\n").map((" " * indent) + _).mkString("\n")

  private def generateBuildSbt(group: String, artifact: String, dependencies: List[Dependency]) = {
    val dependenciesString = dependencies.map { dependency =>
      val separator = if (dependency.isJava) "%" else "%%"
      s""""${dependency.group}" $separator "${dependency.artifact}" % "${dependency.version}""""
    }.mkString(",\n")

    FileStructure.File(
      "build.sbt",
      s"""
organization := "$group"
name := "$artifact"
description := "A very special project generated with zio-start. Good luck!"
version := "0.1.0"
scalaVersion := "2.13.8"

val zioVersion = "${Dependency.zioVersion}"

lazy val core = 
  project
    .in(file("modules/core"))
    .settings(
      name := "$artifact",
      libraryDependencies ++= Seq(
        "dev.zio" %% "zio" % zioVersion,
        "dev.zio" %% "zio-streams" % zioVersion,
        "dev.zio" %% "zio-test" % zioVersion % Test,
        "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
${indent(dependenciesString, 8)}
      ),
      testFrameworks := List(new TestFramework("zio.test.sbt.ZTestFramework"))
    )
             """.trim
    )
  }

  lazy val scalafmtFile =
    FileStructure.File(
      ".scalafmt.conf",
      """
version = 3.0.6
maxColumn = 120
docstrings.wrapMaxColumn = 80
align.preset = most
align.multiline = false
runner.dialect = scala213
rewrite.rules = [RedundantBraces, RedundantParens]
       """.trim
    )
}
