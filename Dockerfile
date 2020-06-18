FROM openjdk:11-jre-slim

RUN apt-get update
RUN apt-get install -y curl

LABEL org.opencontainers.image.title="vernice.veo forms"
LABEL org.opencontainers.image.description="Backend of the verinice.veo-forms web application."
LABEL org.opencontainers.image.ref.name=verinice.veo-forms
LABEL org.opencontainers.image.vendor="SerNet GmbH"
LABEL org.opencontainers.image.authors=verinice@sernet.de
LABEL org.opencontainers.image.licenses=LGPL-3.0
LABEL org.opencontainers.image.source=https://github.com/verinice/verinice-veo-forms

COPY scripts/healthcheck /usr/local/bin/veo-healthcheck

RUN adduser --home /app --disabled-password --gecos '' veo
USER veo
WORKDIR /app

# If by accident we have more than one veo-forms-*.jar docker will complain, which is what we want.
COPY build/libs/veo-forms-*.jar veo-forms.jar

HEALTHCHECK --start-period=15s CMD ["/usr/local/bin/veo-healthcheck"]
EXPOSE 8080
CMD ["java", "-jar", "veo-forms.jar"]
