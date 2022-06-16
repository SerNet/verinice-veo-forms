import com.fasterxml.jackson.databind.ObjectMapper

val domainId = ""

httpGet("/forms/?domainId=$domainId")
    .let{it as List<Map<String, Any>>}
    .forEach{httpDelete("/forms/${it["id"]}")}

getForms()
    .let { ObjectMapper().readValue(it, List::class.java) }
    .map{it as MutableMap<String,Any>}
    .onEach { it["domainId"] = domainId }
    .forEach { httpPost("/forms/", it) }

fun getForms() = """
[] 
"""