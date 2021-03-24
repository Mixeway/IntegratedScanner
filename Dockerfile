FROM ubuntu

ENV DEBIAN_FRONTEND=noninteractive

# Required package installation
RUN env
RUN http_proxy=$HTTP_PROXY apt-get update -y
RUN http_proxy=$HTTP_PROXY apt-get install -y linux-libc-dev
RUN http_proxy=$HTTP_PROXY apt-get install python3-pip -y
RUN http_proxy=$HTTP_PROXY apt-get install openjdk-8-jre-headless maven npm wget -y  && rm -rf /var/lib/apt/lists/*
RUN pip3 install cyclonedx-bom
RUN pip3 install pipreqs
RUN https_proxy=$HTTPS_PROXY wget https://github.com/zricethezav/gitleaks/releases/download/v6.1.2/gitleaks-linux-amd64 -O /bin/gitleaks
RUN https_proxy=$HTTPS_PROXY wget https://github.com/tfsec/tfsec/releases/download/v0.37.0/tfsec-linux-amd64 -O /bin/tfsec
RUN chmod +x /bin/gitleaks
RUN chmod +x /bin/tfsec

#copy certificates
COPY ./utils/ca_tp_pem.crt /root/
COPY ./utils/ca_tp_pem_sha256.crt /root/
COPY ./utils/rootca_pem.crt /root

#Add certs to cacerts
RUN keytool -importcert -file /root/ca_tp_pem.crt -keystore /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/cacerts  -alias "catp" -storepass changeit -noprompt
RUN keytool -importcert -file /root/ca_tp_pem_sha256.crt -keystore /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/cacerts  -alias "catp256" -storepass changeit -noprompt
RUN keytool -importcert -file /root/rootca_pem.crt -keystore /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/cacerts  -alias "rootsignet" -storepass changeit -noprompt

#user
MAINTAINER siewrgrz
RUN adduser mixeway
RUN chown mixeway /bin/gitleaks
USER mixeway

# Building Mixeway Scanner APP
WORKDIR /app
COPY --chown=mixeway ./target/mixewaytesting-1.0.1-SNAPSHOT.jar ./app.jar

ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "/app/app.jar"]