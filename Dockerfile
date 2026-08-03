FROM gcr.io/distroless/java25-debian13:nonroot@sha256:2ce7f9eb870273fcb76131cf1e3b0f3e4ef48ee86679166cae1d53fe035c47aa

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
