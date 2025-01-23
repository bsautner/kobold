package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.ktor.resources.*

object RoutingGenerator {

    fun getRouteClassDeclaration(classDeclaration: KSClassDeclaration): String? {
        val autoRoutingAnnotation = classDeclaration.annotations
            .firstOrNull { it.shortName.asString() == Resource::class.simpleName }

        autoRoutingAnnotation?.arguments?.forEach { argument ->
             if (argument.name?.getShortName() == "path") {
                return argument.value.toString()

            }
        }
        return null
    }

}