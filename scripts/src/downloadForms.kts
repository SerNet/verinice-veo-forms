import com.fasterxml.jackson.databind.ObjectMapper

val domainId = ""

httpGet("/forms/?domainId=$domainId")
    .let { it as List<Map<*, *>> }
    .map { httpGet("/forms/${it["id"]}") }
    .let { ObjectMapper().writeValueAsString(it) }
    .let { println(it) }
