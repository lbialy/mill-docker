import ammonite.ops._
import mill._
import org.scalatest.FreeSpec

import com.github.tobiasjohansson._

class Test extends FreeSpec with MillTest {

  "A DockerModule image" - {

    object imagetest extends AutoModule with DockerModule {
      val name  = "imagetest"
      val tag   = "test"
      val alias = s"$name:$tag"

      override def dockerImageName = name
      override def dockerImageTag  = Some(tag)
      override def dockerContext   = Map[Path, String]()
      override def dockerFile      = """
        FROM alpine
        CMD echo "hi"
      """

    }

    rm(imagetest.millSourcePath)
    mkdir(imagetest.millSourcePath)
    val docker = DockerCli(imagetest.millSourcePath)

    "should be built" in {

      docker.rmi(imagetest.alias)
      assert(!docker.present(imagetest.alias))

      load(imagetest).eval(imagetest.dockerBuild)
      assert(docker.present(imagetest.alias))

    }

    "should be runnable" in {

      load(imagetest).eval(imagetest.dockerBuild)
      assert(docker.run(imagetest.alias).out.trim == "hi")

    }
  }

}

//mkdir(wd / 'src)
//write.over(wd / 'src / "Main.scala", """object Main extends App { println("Main") }""")
