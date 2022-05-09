import com.fasterxml.jackson.databind.ObjectMapper
import java.net.InetSocketAddress
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import kotlin.test.assertEquals

val baseUrl = "https://<YOUR_VEO_INSTANCE_HERE>"
val token =
    "Bearer ey..."

val client = HttpClient.newBuilder().proxy(ProxySelector.of(InetSocketAddress("<YOUR_PROXY_HERE>", 3128))).build()

fun httpGet(route: String, assertStatusCode: Int = 200): Any = client
    .send(
        HttpRequest.newBuilder(URI(baseUrl + route))
            .header("Authorization", token)
            .GET()
            .build(),
        HttpResponse.BodyHandlers.ofString()
    )
    .also { assertEquals(assertStatusCode, it.statusCode()) }
    .body()
    .let { ObjectMapper().readValue(it, Object::class.java) }

fun httpDelete(route: String, assertStatusCode: Int = 204) = client
    .send(
        HttpRequest.newBuilder(URI(baseUrl + route))
            .header("Authorization", token)
            .DELETE()
            .build(),
        HttpResponse.BodyHandlers.ofString()
    )
    .let { assertEquals(assertStatusCode, it.statusCode()) }

fun httpPost(route: String, body: Any? = null, assertStatusCode: Int = 201) = client
    .send(
        HttpRequest.newBuilder(URI(baseUrl + route))
            .header("Authorization", token)
            .header("content-type","application/json")
            .POST(buildBody(body))
            .build(),
        HttpResponse.BodyHandlers.ofString()
    )
    .let { assertEquals(assertStatusCode, it.statusCode()) }

private fun buildBody(body: Any?): BodyPublisher = body
    ?.let { BodyPublishers.ofString(ObjectMapper().writeValueAsString(it)) }
    ?: BodyPublishers.noBody()