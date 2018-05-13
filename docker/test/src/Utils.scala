import ammonite.ops.%%
import ammonite.ops.Path
import ammonite.util.Colors
import mill.define.BaseModule
import mill.define.Discover
import mill.define.Task
import mill.util.PrintLogger

case class DockerCli(path: Path) {

  implicit val wd: Path = path

  def rmi(alias: String) =
    %%("docker", "rmi", "-f", alias)

  def present(alias: String) =
    %%("docker", "images", s"--filter=reference=$alias", "--format={{.ID}}").out.trim.lines.nonEmpty

  def run(alias: String) =
    %%("docker", "run", "--rm", alias)
}

trait MillTest {

  def load(module: BaseModule) = new Mill(module)

  class Mill(val module: BaseModule) {

    import mill.eval._
    import mill.util.Strict.Agg

    val wd = module.millSourcePath

    val evaluator = Evaluator(
      home            = wd / 'home,
      outPath         = wd / 'out,
      externalOutPath = wd / 'out,
      rootModule      = module,
      log = PrintLogger(colored = false,
                        colors     = Colors.BlackWhite,
                        outStream  = System.out,
                        infoStream = System.out,
                        errStream  = System.err,
                        inStream   = System.in)
    )

    def eval(tasks: Task[_]*) = evaluator.evaluate(Agg(tasks: _*))
  }

}

case class BaseDir(path: Path)

object BaseDir {

  implicit def baseDir(implicit
                       file: sourcecode.File,
                       name: sourcecode.FullName): BaseDir = {

    import ammonite.ops._

    val base = Path(file.value).segments.reverse.dropWhile(_ != "src").tail.reverse
    val dir  = RelPath(name.value.replaceAll("\\.", "/"))

    BaseDir(root / RelPath(base, 0) / 'modules / dir)
  }

}

abstract class AutoModule()(implicit val base: BaseDir) extends BaseModule(base.path) {

  lazy val millDiscover = Discover[this.type]

}
