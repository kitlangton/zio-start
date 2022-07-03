package zio.start

final case class Dependency(
  group: String,
  artifact: String,
  version: String,
  description: String,
  url: String,
  nameOverride: Option[String] = None,
  included: List[Dependency] = Nil,
  isJava: Boolean = false
) {
  def name: String = nameOverride.getOrElse(artifact)

  def contains(query0: String): Boolean = {
    val query = query0.toLowerCase
    group.toLowerCase.contains(query) ||
    artifact.toLowerCase.contains(query) ||
    version.toLowerCase.contains(query) ||
    description.toLowerCase.contains(query)
  }
}

object Dependency {

  val zioVersion = "2.0.0"

  val zioJson =
    Dependency(
      group = "dev.zio",
      artifact = "zio-json",
      version = "0.3.0-RC9",
      description = "A performant library for JSON Encoding and Decoding.",
      url = "https://github.com/zio/zio-json"
    )

  val zioHttp =
    Dependency(
      group = "io.d11",
      artifact = "zhttp",
      version = "2.0.0-RC9",
      description = "A supercharged, ergonomic library for building HTTP servers.",
      url = "https://github.com/dream11/zhttp",
      nameOverride = Some("zio-http")
    )

  val zioKafka =
    Dependency(
      group = "dev.zio",
      artifact = "zio-kafka",
      version = "2.0.0",
      description = "A high-performance library for working with Kafka.",
      url = "https://github.com/zio/zio-kafka"
    )

  val zioLogging =
    Dependency(
      group = "dev.zio",
      artifact = "zio-logging",
      version = "2.0.0",
      description = "Powerful logging, compatible with many logging backends out-of-the-box.",
      url = "https://github.com/zio/zio-logging"
    )

  val zioProcess =
    Dependency(
      group = "dev.zio",
      artifact = "zio-process",
      version = "2.0.0",
      description = "A library for working with processes.",
      url = "https://github.com/zio/zio-process"
    )

  val zioCli =
    Dependency(
      group = "dev.zio",
      artifact = "zio-cli",
      version = "0.2.7",
      description = "A library for working with command-line interfaces.",
      url = "https://github.com/zio/zio-cli"
    )

  val zioConfig =
    Dependency(
      group = "dev.zio",
      artifact = "zio-config",
      version = "3.0.1",
      description = "A library for working with configuration.",
      url = "https://github.com/zio/zio-config"
    )

  // TOOD: Waiting for ZIO 2 release
  val caliban =
    Dependency(
      group = "com.github.ghostdogpr",
      artifact = "caliban",
      version = "2.0.0-RC2",
      description = "Functional GraphQL library for Scala.",
      url = "https://github.com/ghostdogpr/caliban"
    )

  val zioQuill =
    Dependency(
      group = "io.getquill",
      artifact = "quill-jdbc-zio",
      version = "4.0.0",
      description = "Compile-time Language Integrated Queries.",
      url = "https://github.com/zio/zio-quill",
      nameOverride = Some("zio-quill"),
      included = List(
        Dependency(
          group = "org.postgresql",
          artifact = "postgresql",
          version = "42.4.0",
          description = "PostgreSQL driver for the Java language.",
          url = "",
          isJava = true
        )
      )
    )

  val zioPrelude =
    Dependency(
      group = "dev.zio",
      artifact = "zio-prelude",
      version = "1.0.0-RC15",
      description = "A lightweight, distinctly Scala take on functional abstractions, with tight ZIO integration.",
      url = "https://github.com/zio/zio-prelude"
    )

  val all: List[Dependency] =
    List(
      zioHttp,
      zioJson,
      zioLogging,
      zioProcess,
      zioQuill,
      zioCli,
      zioConfig,
      zioPrelude,
      // TODO: Waiting for ZIO 2 release
//      caliban,
      zioKafka
    )
}
