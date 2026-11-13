import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import kotlin.concurrent.thread

plugins {
  kotlin("jvm") version "2.4.20"
}

repositories {
  mavenCentral()
  maven { url = uri("https://jitpack.io") }
}

dependencies {
  fun klite(module: String) = "com.github.keksworks.klite:klite-$module:2.1.3"
  implementation(klite("server"))
  implementation(klite("json"))
  implementation(klite("i18n"))
  implementation(klite("jdbc"))
  implementation(klite("oauth"))
  implementation(klite("email"))
  implementation(klite("slf4j"))
  implementation("org.postgresql:postgresql:42.7.14")

  testImplementation(klite("jdbc-test"))
  testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.0.3")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.3")
  testImplementation("ch.tutteli.atrium:atrium-fluent:1.3.0")
  testImplementation("io.mockk:mockk:1.14.11")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(25))
  }
}

sourceSets {
  main {
    kotlin.setSrcDirs(listOf("src"))
    resources.setSrcDirs(listOf("src", "db", "ui/i18n")).exclude("**/*.kt")
  }
  test {
    kotlin.setSrcDirs(listOf("test"))
    resources.setSrcDirs(listOf("test")).exclude("**/*.kt")
  }
}

tasks.test {
  workingDir(rootDir)
  useJUnitPlatform()
  jvmArgs("-DENV=test", "--add-opens=java.base/java.lang=ALL-UNNAMED", "-XX:-OmitStackTraceInFastThrow")
}

tasks.withType<KotlinCompile> {
  compilerOptions {
    freeCompilerArgs.add("-Xreturn-value-checker=check")
  }
  if (System.getProperty("user.name") != "root") finalizedBy("types.ts")
}

tasks.register<Copy>("deps") {
  into("$buildDir/libs/deps")
  from(configurations.runtimeClasspath)
}

val mainClassName = "LauncherKt"

tasks.jar {
  dependsOn("deps")
  doFirst {
    manifest {
      attributes(mapOf(
        "Main-Class" to mainClassName,
        "Class-Path" to File("$buildDir/libs/deps").listFiles()?.joinToString(" ") { "deps/${it.name}"}
      ))
    }
  }
}

val restartMarker = layout.buildDirectory.file("run.marker")

tasks.named("classes") {
  doLast { restartMarker.get().asFile.writeText(System.currentTimeMillis().toString()) }
}

tasks.register("run") {
  description = "Runs the app, restarting it whenever the build finishes"
  group = "application"
  dependsOn("classes")
  doLast {
    fun start() = ProcessBuilder(javaToolchains.launcherFor(java.toolchain).get().executablePath.toString(),
      "--add-exports=java.base/sun.net.www=ALL-UNNAMED",
      "-cp", sourceSets.main.get().runtimeClasspath.asPath, mainClassName)
      .directory(projectDir).redirectErrorStream(true).start()
      .also { app -> thread(isDaemon = true) { app.inputStream.copyTo(System.out) } }

    val marker = restartMarker.get().asFile
    var last = marker.lastModified()
    var app = start()
    try {
      while (true) {
        Thread.sleep(500)
        val modified = marker.lastModified()
        if (modified == last) continue
        last = modified
        logger.lifecycle("Build finished, restarting")
        app.apply { toHandle().destroy(); waitFor() }
        app = start()
      }
    } finally {
      app.apply { toHandle().destroy(); waitFor() }
    }
  }
}

tasks.register<JavaExec>("types.ts") {
  dependsOn("classes")
  mainClass.set("klite.json.TSGenerator")
  classpath = sourceSets.main.get().runtimeClasspath
  args("${project.buildDir}/classes/kotlin/main", "-r", "Routes")
  standardOutput = ByteArrayOutputStream()
  doLast {
    project.file("ui/src/api/types.ts").writeText(standardOutput.toString().replace("TSID", "Id"))
  }
}
