import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.slf4j.LoggerFactory
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgisContainerProvider
import org.testcontainers.containers.output.Slf4jLogConsumer

class PostgreSQLExtension : BeforeAllCallback, AfterAllCallback {
  private val log = LoggerFactory.getLogger(PostgreSQLExtension::class.simpleName)

  private lateinit var postgis: JdbcDatabaseContainer<*>

  override fun beforeAll(context: ExtensionContext) {
    val imageTag = "17-3.5-alpine"
    postgis = PostgisContainerProvider().newInstance(imageTag)
      .withLogConsumer(Slf4jLogConsumer(log))
      .withDatabaseName("feedless-test")
      .withUsername("postgres")
      .withPassword("admin")
      .withReuse(false)

    postgis.start()

    println(postgis.jdbcUrl)
    val jdbcUrl =
      "jdbc:tc:postgis:$imageTag://localhost:${postgis.firstMappedPort}/${postgis.databaseName}?TC_REUSABLE=false"
    System.setProperty("spring.datasource.url", jdbcUrl)
    System.setProperty("spring.datasource.username", postgis.username)
    System.setProperty("spring.datasource.password", postgis.password)
    System.setProperty("spring.flyway.baseline-on-migrate", "true")
    System.setProperty("spring.flyway.baseline-version", "1")
//    System.setProperty("spring.flyway.target", "72")
    System.setProperty("spring.flyway.enabled", "true")
  }

  override fun afterAll(context: ExtensionContext) {
    // when using podman, either explicitly stop() the container or set reuse=true, cause podman runs with disabled ryuk TESTCONTAINERS_RYUK_DISABLED
//    Runtime.getRuntime().addShutdownHook(Thread { postgis.stop() })
    postgis.stop()
//    postgis.close()
  }
}

/*

  companion object {

    @Container
    private val postgres = PostgreSQLContainer("postgres:16")
      .withDatabaseName("feedless-test")
      .withUsername("postgres")
      .withPassword("admin")

    @JvmStatic
    @DynamicPropertySource
    fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
      registry.add("spring.datasource.url") { "jdbc:tc:postgresql:16://localhost:${postgres.firstMappedPort}/${postgres.databaseName}?TC_REUSABLE=false" }
      registry.add("spring.datasource.username", postgres::getUsername)
      registry.add("spring.datasource.password", postgres::getPassword)
      registry.add("spring.jpa.hibernate.ddl-auto") { "create" }
    }
  }
 */
