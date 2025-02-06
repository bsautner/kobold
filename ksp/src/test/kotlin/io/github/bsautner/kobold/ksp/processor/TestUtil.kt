

fun readResourceFile(resourcePath: String): String =
	object {}.javaClass.getResource(resourcePath)?.readText(Charsets.UTF_8)
		?: error("Resource not found: $resourcePath")