FROM gcr.io/distroless/java25-debian13:nonroot@sha256:ca60da1345c0f17b6d019049e6749e15f10fd3c0da86dec938d2b4ec565d0629

ARG VEO_FORMS_VERSION

LABEL org.opencontainers.image.title="vernice.veo forms"
LABEL org.opencontainers.image.description="Backend of the verinice.veo-forms web application."
LABEL org.opencontainers.image.ref.name=verinice.veo-forms
LABEL org.opencontainers.image.vendor="SerNet GmbH"
LABEL org.opencontainers.image.authors=verinice@sernet.de
LABEL org.opencontainers.image.licenses=AGPL-3.0
LABEL org.opencontainers.image.source=https://github.com/verinice/verinice-veo-forms

ENV JDK_JAVA_OPTIONS "-Djdk.serialFilter=maxbytes=0"

USER nonroot

COPY --chown=nonroot:nonroot build/libs/veo-forms-${VEO_FORMS_VERSION}.jar /app/veo-forms.jar

WORKDIR /app
EXPOSE 8080
CMD ["veo-forms.jar"]
