import mill._
import mill.scalalib._
import mill.define.TaskModule

object docker extends ScalaModule {
  def scalaVersion = "2.12.4"
  def ivyDeps = Agg(
    ivy"com.lihaoyi::mill-dev:0.2.0-11-13d026"
  )

  object test extends Tests {
    def scalaVersion = docker.scalaVersion
    def moduleDeps = Seq(docker)
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.0.4",
      ivy"com.lihaoyi::sourcecode:0.1.4"
    )
    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }

}

