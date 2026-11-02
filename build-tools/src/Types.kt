package buildtools

import org.jetbrains.amper.plugins.Classpath
import org.jetbrains.amper.plugins.CompilationArtifact
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeText

/** Generates TypeScript types for the UI from the routes of the compiled application */
@TaskAction
fun generateTypes(
  @Input classes: CompilationArtifact,
  @Input runtimeClasspath: Classpath,
  @Output typesFile: Path,
) {
  val java = Path.of(System.getProperty("java.home"), "bin", "java")
  val classpath = runtimeClasspath.resolvedFiles.joinToString(File.pathSeparator)
  val process = ProcessBuilder(java.toString(), "-cp", classpath, "klite.json.TSGenerator",
    classes.artifact.toString(), "-r", "Routes")
    .redirectError(ProcessBuilder.Redirect.INHERIT)
    .start()
  val types = process.inputStream.bufferedReader().readText()
  val exitCode = process.waitFor()
  check(exitCode == 0) { "TypeScript types generation failed with exit code $exitCode" }

  typesFile.createParentDirectories().writeText(types.replace("TSID", "Id"))
}
