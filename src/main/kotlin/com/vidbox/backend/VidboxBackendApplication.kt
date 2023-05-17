package com.vidbox.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VidboxBackendApplication

fun main(args: Array<String>) {
	runApplication<VidboxBackendApplication>(*args)
}
