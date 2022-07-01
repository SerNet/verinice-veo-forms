FROM gcr.io/distroless/java17-debian11:nonroot

ARG VEO_FORMS_VERSION

LABEL org.opencontainers.image.title="vernice.veo forms"
LABEL org.opencontainers.image.description="Backend of the verinice.veo-forms web application."
LABEL org.opencontainers.image.ref.name=verinice.veo-forms
LABEL org.opencontainers.image.vendor="SerNet GmbH"
LABEL org.opencontainers.image.authors=verinice@sernet.de
LABEL org.opencontainers.image.licenses=AGPL-3.0
LABEL org.opencontainers.image.source=https://github.com/verinice/verinice-veo-forms

USER nonroot

COPY --chown=nonroot:nonroot build/libs/veo-forms-${VEO_FORMS_VERSION}.jar /app/veo-forms.jar

WORKDIR /app
EXPOSE 8080
CMD ["veo-forms.jar"]
