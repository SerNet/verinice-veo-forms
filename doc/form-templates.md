## Form templates

Form templates can be used to automatically distribute pre-built forms to multiple clients. Form templates are organized
in versioned form template bundles that exist globally for all clients and target a certain version of a domain
template. When a new domain is created, veo-forms will look up the latest form template bundle for that domain's domain
template version and incarnate its form templates within that new domain.

### Publishing form templates along with a new domain template version

The domain template version for which form templates are to be created must always exist first. A content creator may
work in a domain and make local changes both to the domain itself in veo and to the forms in veo-forms in arbitrary
order. But when it is time for publishing, the local domain must always be published as a new domain template before the
local forms are published as a new form template bundle, because a form template bundle always depends on a domain
template and the new form template bundle must point to the correct new domain template ID. If the domain was already based on
a form template bundle for a previous version of the domain template, the new form template bundle gets a new minor
release version number (e.g. 1.1.0). If this is the initial form template bundle, it becomes version 1.0.0.

### Publishing form templates only

It is also possible for a content creator to release a new form template update in veo-forms without an accompanying new
domain template version, e.g. if the object schema in the latest domain template is correct but a problem must be fixed
in a form template. In that case a new form template bundle can be created with a reference to the same old domain
template version, resulting in a form template bundle tagged with a new patch version (e.g. 1.0.1).

### Validation rules

To avoid conflicts, it is not permitted to create a form template bundle from an outdated domain, i.e. a domain that is
not based on the latest available form template bundle.

### Test example

See templating in action: [TemplatingMvcTest](../src/test/kotlin/org/veo/forms/mvc/TemplatingMvcTest.kt)
