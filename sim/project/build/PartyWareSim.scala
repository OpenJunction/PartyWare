import sbt._
import sbt.FileUtilities._
import java.io.File

class PartyWareSim(info: ProjectInfo) extends DefaultProject(info){

  import Configurations.{Compile, CompilerPlugin, Default, Provided, Runtime, Test}
  val scalaToolsSnapshots = "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"
  val jbossRepo = "JBoss Maven 2 Repo" at "http://repository.jboss.org/maven2"
  val xpp = "xpp3" % "xpp3" % "1.1.4c"

}

