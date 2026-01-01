package me.dvyy.particles.schema

import com.mineinabyss.jsonschema.dsl.jsonSchema
import com.mineinabyss.jsonschema.generator.KotlinxSerializationJsonSchemaGenerator
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import me.dvyy.particles.dsl.ParticlesConfig
import kotlin.io.path.*

fun main(args: Array<String>) {
    val outputPath = args.firstOrNull()
    val json = Json { prettyPrint = true }
    if (outputPath == null) {
        println(json.encodeToString(particlesSchema))
        return
    }

    val output = Path(outputPath).createParentDirectories()
    if (output.notExists()) output.createFile()
    output.outputStream().use {
        json.encodeToStream(particlesSchema, it)
    }
}

val particlesSchema = jsonSchema {
    val generator = KotlinxSerializationJsonSchemaGenerator()
    provideDefinitions {
        generator.applyClassDescriptor(it)
    }
    rootProperty {
        ref = definition<ParticlesConfig>()
    }
}
