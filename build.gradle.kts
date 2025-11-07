import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
  kotlin("jvm") version "2.2.0"
}

repositories {
  mavenCentral()
  maven { url = uri("https://jitpack.io") }
}

dependencies {
  fun klite(module: String) = "com.github.codeborne.klite:klite-$module:1.7.0"
  implementation(klite("server"))
  implementation(klite("json"))
  implementation(klite("i18n"))
  implementation(klite("jdbc"))
  implementation(klite("slf4j"))
  implementation("org.postgresql:postgresql:42.7.7")

  testImplementation(klite("jdbc-test"))
  testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.4")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.4")
  testImplementation("ch.tutteli.atrium:atrium-fluent:1.3.0-alpha-2")
  testImplementation("io.mockk:mockk:1.14.6")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
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
  // enable JUnitAssertionImprover from klite.jdbc-test
  jvmArgs("-DENV=test", "-Djunit.jupiter.extensions.autodetection.enabled=true", "--add-opens=java.base/java.lang=ALL-UNNAMED", "-XX:-OmitStackTraceInFastThrow")
}

tasks.withType<KotlinCompile> {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_21)
    if (System.getProperty("user.name") != "root") finalizedBy("types.ts")
  }
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
      attributes(
        "Main-Class" to mainClassName,
        "Class-Path" to File("$buildDir/libs/deps").listFiles()?.joinToString(" ") { "deps/${it.name}"}
      )
    }
  }
}

tasks.register<JavaExec>("run") {
  workingDir(rootDir)
  jvmArgs("--add-exports=java.base/sun.net.www=ALL-UNNAMED")
  mainClass.set(mainClassName)
  classpath = sourceSets.main.get().runtimeClasspath
}

tasks.register<JavaExec>("types.ts") {
  dependsOn("classes")
  mainClass.set("klite.json.TSGenerator")
  classpath = sourceSets.main.get().runtimeClasspath
  args("${project.buildDir}/classes/kotlin/main")
  standardOutput = ByteArrayOutputStream()
  doLast {
    project.file("ui/src/api/types.ts").writeText("""
      export type Id<T extends Entity<T>> = string & {_of?: T}
      export type Entity<T extends Entity<T>> = {id: Id<T>}
      """.trimIndent() + "\n\n" + standardOutput.toString().replace("TSID", "Id"))
  }
}
