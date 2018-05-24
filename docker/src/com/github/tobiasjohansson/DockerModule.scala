package com.github.tobiasjohansson

import ammonite.ops._

import mill._
import mill.scalalib._
import mill.define.{Target => T, Command => C, _}

trait DockerModule extends Module {

  def dockerImageName: T[String]
  def dockerFile:      T[String]

  def dockerImageTag:        T[Option[String]] = None
  def dockerImageRepository: T[Option[String]] = None

  def dockerImageAlias: T[String] = T {
    dockerImageRepository().fold("")(_ + "/") + dockerImageName() + dockerImageTag().fold("")(
      ":" + _)
  }

  def dockerBuildCommandArgs: T[Seq[String]] =
    T { Seq("-q", "-t", dockerImageAlias()) }

  def dockerBuildCommand: T[Seq[String]] =
    T { Seq(Seq("docker", "build"), dockerBuildCommandArgs(), Seq(".")).flatten }

  def dockerContext: T[Map[Path, String]] =
    T { Map[Path, String]() }

  def dockerBuild: T[String] = T {

    implicit val wd = T.ctx().dest

    write(wd / 'Dockerfile, dockerFile())

    for ((src, trg) <- dockerContext()) {
      val dst = wd / RelPath(trg)
      mkdir(dst / up)
      cp(src, dst)
    }

    %%(dockerBuildCommand()).out.string.trim
  }

  def dockerRun(): C[Unit] = T.command {
    implicit val wd = T.ctx().dest
    print(%%("docker", "run", "--rm", dockerBuild()).out.string)
  }
}

trait ScalaDockerModule extends DockerModule {
  this: ScalaModule =>

  def dockerImageName:  T[String]      = T { artifactId() }
  def dockerFrom:       T[String]      = T { "openjdk:8-slim" }
  def dockerMaintainer: T[String]      = T { "" }
  def dockerCmd:        T[Seq[String]] = T { Seq("java", "-jar", "assembly.jar") }

  override def dockerContext: T[Map[Path, String]] = T {
    Map(assembly().path -> s"${artifactId()}.jar")
  }

  def dockerFile: T[String] = T {
    s"""
    FROM ${dockerFrom()}
    LABEL maintainer="${dockerMaintainer()}"

    ${dockerContext().values
      .map(v => s"COPY $v $v")
      .mkString("\n")}

    CMD ${dockerCmd().mkString(" ")}
  """
  }
}
