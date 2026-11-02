package buildtools

import org.jetbrains.amper.plugins.Classpath
import org.jetbrains.amper.plugins.CompilationArtifact
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import java.nio.file.Path
import java.util.jar.Attributes
import java.util.jar.Attributes.Name.CLASS_PATH
import java.util.jar.Attributes.Name.MAIN_CLASS
import java.util.jar.Attributes.Name.MANIFEST_VERSION
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.zip.ZipFile
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

/** Creates a thin runnable jar in `build/libs` with dependencies next to it, ready for `java -jar` */
@TaskAction
@OptIn(ExperimentalPathApi::class)
fun dist(
  @Input appJar: CompilationArtifact,
  @Input runtimeClasspath: Classpath,
  @Output distDir: Path,
  mainClass: String?,
) {
  distDir.deleteRecursively()
  val depsDir = distDir.resolve("deps").createDirectories()
  runtimeClasspath.resolvedFiles.filter { it != appJar.artifact && it.name != appJar.artifact.name }.forEach {
    it.toFile().copyTo(depsDir.resolve(it.name).toFile(), overwrite = true)
  }

  val classPath = depsDir.listDirectoryEntries().sorted().joinToString(" ") { "deps/${it.name}" }
  val manifest = Manifest().apply {
    mainAttributes[MANIFEST_VERSION] = "1.0"
    mainAttributes[MAIN_CLASS] = mainClass ?: error("mainClass is not set")
    mainAttributes[CLASS_PATH] = classPath
  }

  val target = distDir.resolve("app.jar")
  JarOutputStream(target.toFile().outputStream(), manifest).use { out ->
    ZipFile(appJar.artifact.toFile()).use { zip ->
      zip.entries().asSequence()
        .filter { !it.isDirectory && it.name != "META-INF/MANIFEST.MF" }
        .forEach { entry ->
          out.putNextEntry(JarEntry(entry.name))
          zip.getInputStream(entry).copyTo(out)
          out.closeEntry()
        }
    }
  }
}
