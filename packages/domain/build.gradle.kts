plugins {
  alias(libs.plugins.kotlin.jvm)
  // opens @Service/@Transactional/@Cacheable classes for CGLIB proxies
  alias(libs.plugins.kotlin.spring)
  kotlin("plugin.serialization") version "1.9.0"
  `java-test-fixtures`
}

repositories {
  mavenCentral()
}

kotlin { jvmToolchain(21) }

dependencies {
  // api: consumers resolve the versionless Spring artifacts below
  api(platform(libs.spring.boot.bom))
  testFixturesImplementation(platform(libs.spring.boot.bom))

  implementation(kotlin("stdlib"))
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  api(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.json)
  api(libs.gson)
  api(libs.hibernate.spatial)
  api(libs.jsoup)
  implementation(libs.commons.lang3)
  implementation(libs.tika.core)
  implementation(libs.spring.boot.validation)
  api("org.springframework.data:spring-data-commons")
  implementation("org.springframework:spring-web")
  implementation("jakarta.annotation:jakarta.annotation-api")
  // TooManyConnectionsPerHostException check only; server-core ships the client
  compileOnly(libs.async.http.client)
  api("org.springframework:spring-context")
  api("org.springframework:spring-tx")
  api("org.springframework.security:spring-security-core")
  api("org.springframework.security:spring-security-oauth2-client")
  api("org.springframework.security:spring-security-oauth2-jose")
  api("io.micrometer:micrometer-core")
  api("com.fasterxml.jackson.core:jackson-annotations")
  implementation("org.slf4j:slf4j-api")

  testFixturesImplementation("org.mockito:mockito-core")

  testImplementation(kotlin("test"))
  testImplementation(libs.kotlinx.coroutines.test)
  // DocumentUseCase's exception check loads the class at runtime
  testImplementation(libs.async.http.client)
  testImplementation(libs.spring.boot.test)
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

tasks.test {
  useJUnitPlatform()
}
