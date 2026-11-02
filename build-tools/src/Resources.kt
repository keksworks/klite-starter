package buildtools

import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.deleteRecursively

/** Puts DB migrations and i18n translations from their locations into the module resources */
@TaskAction
@OptIn(ExperimentalPathApi::class)
fun copyResources(
  @Input dbDir: Path,
  @Input i18nDir: Path,
  @Output outputDir: Path,
) {
  outputDir.deleteRecursively()
  listOf(dbDir, i18nDir).forEach { it.copyToRecursively(outputDir, followLinks = false) }
}
