package io.github.bsautner.utils.io.github.bsautner.kobold.ksp

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import io.github.bsautner.kobold.KoboldStatic
import io.github.bsautner.ksp.processor.ClassHelper
import io.ktor.resources.Resource
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@TestInstance(Lifecycle.PER_METHOD)
class ClassHelperTest {

	val classHelper = ClassHelper()

	@Resource("/") @KoboldStatic("test")
	class StaticTest

	@Test
	fun `returns serializableResponse class when provided in Kobold annotation`() {
		// Create a fake KSName for "Kobold" (used in the annotation)
		val koboldKSName = mockk<KSName>()
		every { koboldKSName.asString() } returns "Kobold"

		// Create a fake KSName for "serializableResponse" (argument name)
		val argKSName = mockk<KSName>()
		every { argKSName.getShortName() } returns "serializableResponse"

		// Create a fake KSValueArgument for the serializableResponse argument.
		val fakeArgument = mockk<KSValueArgument>()
		every { fakeArgument.name } returns argKSName

		// Create a fake KSType whose declaration will be returned.
		val fakeKSType = mockk<KSType>()
		val expectedDeclaration = mockk<KSClassDeclaration>()
		every { fakeKSType.declaration } returns expectedDeclaration
		every { fakeArgument.value } returns fakeKSType

		// Create a fake KSAnnotation for @Kobold containing the argument.
		val koboldAnnotation = mockk<KSAnnotation>()
		val koboldAnnotationName = mockk<KSName>()
		every { koboldAnnotationName.asString() } returns "Kobold"
		every { koboldAnnotation.shortName } returns koboldAnnotationName
		every { koboldAnnotation.arguments } returns listOf(fakeArgument)

		// Create a fake KSClassDeclaration with the @Kobold annotation.
		val classDeclaration = mockk<KSClassDeclaration>()
		every { classDeclaration.annotations } returns listOf(koboldAnnotation).asSequence()

		// When the serializableResponse argument is provided, the function should return its class.
		val result = classHelper.getSerializableClassDeclaration(classDeclaration)
		assertEquals(expectedDeclaration, result)
	}

	@Test
	fun `returns classDeclaration when no serializableResponse but class is annotated with Serializable and has a Kobold annotation`() {
		// Create fake KSNames for annotations.
		val koboldKSName = mockk<KSName>()
		every { koboldKSName.asString() } returns "Kobold"
		val serializableKSName = mockk<KSName>()
		every { serializableKSName.asString() } returns "Serializable"

		// Create a fake KSAnnotation for @Kobold without any arguments.
		val koboldAnnotation = mockk<KSAnnotation>()
		val koboldAnnotationName = mockk<KSName>()
		every { koboldAnnotationName.asString() } returns "Kobold"
		every { koboldAnnotation.shortName } returns koboldAnnotationName
		every { koboldAnnotation.arguments } returns emptyList()

		// Create a fake KSAnnotation for @Serializable.
		val serializableAnnotation = mockk<KSAnnotation>()
		val serializableAnnotationName = mockk<KSName>()
		every { serializableAnnotationName.asString() } returns "Serializable"
		every { serializableAnnotation.shortName } returns serializableAnnotationName

		// Create a fake KSClassDeclaration that has both annotations.
		val classDeclaration = mockk<KSClassDeclaration>()
		every { classDeclaration.annotations } returns sequenceOf(koboldAnnotation, serializableAnnotation)

		// The function should return the classDeclaration itself.
		val result = classHelper.getSerializableClassDeclaration(classDeclaration)
		assertEquals(classDeclaration, result)
	}

	@Test
	fun `returns classDeclaration when there is no Kobold annotation but class is annotated with Serializable`() {
		// Create a fake KSAnnotation for @Serializable.
		val serializableAnnotation = mockk<KSAnnotation>()
		val serializableAnnotationName = mockk<KSName>()
		every { serializableAnnotationName.asString() } returns "Serializable"
		every { serializableAnnotation.shortName } returns serializableAnnotationName

		// Create a fake KSClassDeclaration that has only the @Serializable annotation.
		val classDeclaration = mockk<KSClassDeclaration>()
		every { classDeclaration.annotations } returns sequenceOf(serializableAnnotation)

		// The function should return the classDeclaration itself.
		val result = classHelper.getSerializableClassDeclaration(classDeclaration)
		assertEquals(classDeclaration, result)
	}

	@Test
	fun `returns null when neither Kobold nor Serializable annotations are present`() {
		// Create a fake KSAnnotation that is not Kobold or Serializable.
		val otherAnnotation = mockk<KSAnnotation>()
		val otherAnnotationName = mockk<KSName>()
		every { otherAnnotationName.asString() } returns "OtherAnnotation"
		every { otherAnnotation.shortName } returns otherAnnotationName

		// Create a fake KSClassDeclaration with only the other annotation.
		val classDeclaration = mockk<KSClassDeclaration>()
		every { classDeclaration.annotations } returns sequenceOf(otherAnnotation)

		// The function should return null.
		val result = classHelper.getSerializableClassDeclaration(classDeclaration)
		assertNull(result)
	}

}