/*
 *
 *  * Copyright (c) 2025 Benjamin Sautner
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

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