package zio.start

object Dependency {
  val zioJson =
    Dependency(
      group = "dev.zio",
      artifact = "zio-json",
      version = "0.3.0",
      description = "A performant library for JSON Encoding and Decoding."
    )

  val zioHttp =
    Dependency(
      group = "dev.zio",
      artifact = "zio-http",
      version = "0.3.0",
      description = "A supercharged, ergonomic library for building HTTP servers."
    )

  val zioKafka =
    Dependency(
      group = "dev.zio",
      artifact = "zio-kafka",
      version = "0.3.0",
      description = "A high-performance library for working with Kafka."
    )

  val all: List[Dependency] =
    List(
      zioHttp,
      zioJson,
      zioKafka
    )
}

final case class Dependency(
  group: String,
  artifact: String,
  version: String,
  description: String
) {
  def contains(query0: String): Boolean = {
    val query = query0.toLowerCase
    group.toLowerCase.contains(query) ||
    artifact.toLowerCase.contains(query) ||
    version.toLowerCase.contains(query) ||
    description.toLowerCase.contains(query)
  }
}
