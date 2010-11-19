import sbt._
import sbt.FileUtilities._
import java.io.File

class PartyWareSim(info: ProjectInfo) extends DefaultProject(info){

  import Configurations.{Compile, CompilerPlugin, Default, Provided, Runtime, Test}
  val scalaToolsSnapshots = "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"
  val stanfordRepo = "Stanford Maven 2 Repo" at "http://prpl.stanford.edu:8081/nexus/content/groups/public"
  val junction = "edu.stanford.prpl.junction" % "JAVAJunction" % "0.6.7-SNAPSHOT"

}

