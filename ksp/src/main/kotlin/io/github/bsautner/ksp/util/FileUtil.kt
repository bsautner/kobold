package io.github.bsautner.ksp.util

import java.io.File

fun File.parentDir() : File {

	return File(this.parent)

}

fun File.isEmpty() : Boolean {
	if (this.isFile) {
		return false
	} else {
		return  this.listFiles().isEmpty()
	}
}

fun File.notExist() : Boolean {
	return (! this.exists())
}

fun File.parentFile(): File? = this.parent?.let { File(it) }

fun touchFile(file: File) : File {

	if (!file.exists()) {
		file.createNewFile()
	} else {
		file.setLastModified(System.currentTimeMillis())
	}
	return file

}