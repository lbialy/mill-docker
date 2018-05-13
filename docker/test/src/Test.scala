import ammonite.ops._
import mill._
import org.scalatest.FreeSpec

import com.github.tobiasjohansson._

class Test extends FreeSpec with MillTest {

  "A DockerModule image" - {

    object imagetest extends AutoModule with DockerModule {
      override def dockerImageName = "imagetest"
      override def dockerImageTag  = Some("test")
      override def dockerContext   = Map[Path, String]()
      override def dockerCommands  = """
        FROM alpine
        CMD echo "hi"
      """

    }

    mkdir(imagetest.millSourcePath)
    val docker = DockerCli(imagetest.millSourcePath)

    "should be built" in {

      docker.rmi(imagetest.dockerImageAlias)
      assert(!docker.present(imagetest.dockerImageAlias))

      load(imagetest).eval(imagetest.dockerBuild)
      assert(docker.present(imagetest.dockerImageAlias))

    }

    "should be runnable" in {

      load(imagetest).eval(imagetest.dockerBuild)
      assert(docker.run(imagetest.dockerImageAlias).out.trim == "hi")

    }
  }

}

//mkdir(wd / 'src)
//write.over(wd / 'src / "Main.scala", """object Main extends App { println("Main") }""")
