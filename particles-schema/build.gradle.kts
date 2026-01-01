import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        freeCompilerArgs.add("-Xcontext-parameters")
    }
    jvm {
    }
    jvmToolchain(21)

    js {
        binaries.executable()
        browser {
            @OptIn(ExperimentalDistributionDsl::class)
            distribution {
                outputDirectory.set(File("${rootDir}/dist/js"))
            }
            commonWebpackConfig {
                //mode = KotlinWebpackConfig.Mode.PRODUCTION
                mode = KotlinWebpackConfig.Mode.DEVELOPMENT
            }
        }
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            target.set("es2015")
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":particles-kool"))
                implementation(libs.kotlinx.serialization.json)
                implementation("com.mineinabyss.jsonschema-kt:dsl:0.1.1")
                implementation("com.mineinabyss.jsonschema-kt:generator-kotlinx-serialization:0.1.1")
            }
        }
    }
}


tasks {
    val generateConfigSchema by registering(JavaExec::class) {
        classpath = sourceSets["jvmMain"].runtimeClasspath
        val outputFile = rootProject.file("examples/schema.json")
        args(outputFile.absolutePath)
        mainClass.set("me.dvyy.particles.schema.SchemaGeneratorKt")
        inputs.dir(rootProject.file("particles-config/src"))
        outputs.file(outputFile)
    }
}
