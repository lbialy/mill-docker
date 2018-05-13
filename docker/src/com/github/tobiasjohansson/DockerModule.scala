package com.github.tobiasjohansson

import mill._
import mill.scalalib._

trait DockerModule extends Module {

  import ammonite.ops._
  import mill.define._

  def dockerImageName: String
  def dockerImageTag: Option[String]        = None
  def dockerImageRepository: Option[String] = None

  def dockerImageAlias: String =
    dockerImageRepository.fold("")(_ + "/") + dockerImageName + dockerImageTag.fold("")(":" + _)

  def dockerBuildCommandArgs: Seq[String] = Seq("-q", "-t", dockerImageAlias)

  def dockerBuildCommand: Seq[String] =
    Seq(
      Seq("docker", "build"),
      dockerBuildCommandArgs,
      Seq(".")
    ).flatten

  def dockerContext: Target[Map[Path, String]]
  def dockerCommands: Target[String]

  def dockerBuild: Target[String] = T {

    implicit val wd = T.ctx().dest

    write(wd / 'Dockerfile, dockerCommands())

    for ((src, trg) <- dockerContext()) {
      val dst = wd / RelPath(trg)
      mkdir(dst / up)
      cp(src, dst)
    }

    %%(dockerBuildCommand).out.string.trim
  }

  def dockerRun() = T.command {
    implicit val wd = T.ctx().dest
    print(%%("docker", "run", "--rm", dockerBuild()).out.string)
  }
}

trait ScalaDockerModule extends DockerModule {
  this: ScalaModule =>

  def dockerFrom: String       = "openjdk:8-slim"
  def dockerMaintainer: String = ""

  def dockerCmd: Seq[String] = Seq(
    "java",
    "-jar",
    "assembly.jar"
  )

  def dockerContext = T {
    Map(
      assembly().path -> "assembly.jar"
    )
  }

  def dockerCommands = T {
    s"""
    FROM ${dockerFrom}
    LABEL maintainer="${dockerMaintainer}"

    ${dockerContext().values
      .map(v => s"COPY $v $v")
      .mkString("\n")}

    CMD ${dockerCmd.mkString(" ")}
  """
  }
}
