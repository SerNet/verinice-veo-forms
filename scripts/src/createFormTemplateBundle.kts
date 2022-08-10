val domainId = "d0ed41d4-91d9-4ed2-b3f6-b5d376a24a38"
val domainTemplateId = "7b3ec3d5-03f0-5541-80cd-68d339e92903"

httpPost("/forms/form-template-bundles/create-from-domain?domainId=$domainId&domainTemplateId=$domainTemplateId", null)
