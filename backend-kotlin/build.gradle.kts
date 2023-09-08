import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.12-SNAPSHOT"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
}

group = "com.vidbox"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
	google()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
	runtimeOnly("org.postgresql:postgresql")
	implementation("com.google.apis:google-api-services-gmail:v1-rev20220404-2.0.0")
	//implementation("com.google.auth:google-auth-library-oauth2-http:1.3.0")
	implementation("com.google.api-client:google-api-client:1.33.0")
	implementation(platform("com.google.cloud:libraries-bom:25.1.0"))
	implementation("com.google.cloud:google-cloud-storage")
	implementation("com.google.guava:guava:31.1-jre")
	implementation("com.google.firebase:firebase-admin:9.2.0")
	implementation("org.springframework.boot:spring-boot-devtools")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.google.cloud.sql:postgres-socket-factory:1.13.1")
	implementation("com.google.code.gson:gson:2.8.9")
	implementation("com.google.cloud:google-cloud-secretmanager:2.22.0")
	implementation("org.flywaydb:flyway-core:9.1.2")
	implementation("com.squareup.okhttp3:okhttp:4.9.1")
	implementation("com.google.cloud:spring-cloud-gcp-starter:4.7.1")
	implementation("com.google.cloud:spring-cloud-gcp-starter-secretmanager:4.7.1")
	implementation("commons-cli:commons-cli:1.4")
	implementation("org.jsoup:jsoup:1.14.3")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.register("runCli", JavaExec::class) {
	mainClass.set("com.vidbox.backend.cli.MainKt") // Replace with the full path to your CLI main class
	classpath = sourceSets["main"].runtimeClasspath
	args = project.properties["cliArgs"]?.toString()?.split(",") ?: listOf()
}


tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
