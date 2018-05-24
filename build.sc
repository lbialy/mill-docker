import mill._
import mill.scalalib._
import mill.scalalib.publish.{Developer, PomSettings, VersionControl}

object docker extends ScalaModule with PublishModule {

  def publishVersion = "0.1.0"
  def scalaVersion   = "2.12.4"
  def pomSettings = PomSettings(
    description  = "Docker support in Mill build tool",
    organization = "com.github.tobiasjohansson",
    url          = "https://github.com/tobias-johansson/mill-docker",
    versionControl = VersionControl.github(
      owner = "tobias-johansson",
      repo  = "mill-docker"),
    developers = Seq(Developer(
      id   = "tobias-johansson",
      name = "Tobias Johansson",
      url  = "https://github.com/tobias-johansson")),
    licenses   = Seq(),
  )
  def compileIvyDeps = Agg(
    ivy"com.lihaoyi::mill-dev:0.2.2"
  )

  object test extends Tests {
    override def scalaVersion = docker.scalaVersion
    override def moduleDeps   = Seq(docker)
    override def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.0.4",
      ivy"com.lihaoyi::sourcecode:0.1.4"
    )
    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }

}

