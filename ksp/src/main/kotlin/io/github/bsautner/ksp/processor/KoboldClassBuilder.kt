package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import io.github.bsautner.ksp.routing.RoutingGenerator.getRouteClassDeclaration

interface KoboldClassBuilder {

//	fun create(sequence: Sequence<KSAnnotated>)
//	fun addImports(builder: FileSpec.Builder, sequence: Sequence<KSAnnotated>)
//	fun generate(sequence: Sequence<KSAnnotated>): CodeBlock

}
