FROM maven:3.6-jdk-8 as maven
WORKDIR /app


COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./src ./src


RUN mvn package -DskipTests && cp target/mixeway*.jar app.jar

FROM ubuntu:latest

ENV DEBIAN_FRONTEND=noninteractive

# Required package installation
RUN env
RUN apt clean && apt autoclean
RUN apt-get update -y
RUN apt-get install git -y
RUN apt-get install -y linux-libc-dev
RUN apt-get install python3-pip -y
RUN apt-get install zip unzip -y
RUN apt-get install openjdk-8-jdk --fix-missing -y
RUN apt-get install maven -y
RUN apt-get install npm -y
RUN apt-get install wget -y
RUN rm -rf /var/lib/apt/lists/*
RUN pip3 install cyclonedx-bom
RUN pip3 install pipreqs
RUN wget https://github.com/zricethezav/gitleaks/releases/download/v6.1.2/gitleaks-linux-amd64 -O /bin/gitleaks
RUN wget https://github.com/tfsec/tfsec/releases/download/v0.37.0/tfsec-linux-amd64 -O /bin/tfsec
RUN wget https://github.com/Checkmarx/kics/releases/download/v1.3.2/kics_1.3.2_linux_x64.tar.gz -O /tmp/kics
RUN tar -xvf /tmp/kics
RUN cp kics /bin/kics
RUN chmod +x /bin/kics
RUN chmod +x /bin/gitleaks
RUN chmod +x /bin/tfsec
#RUN npm install -g npm
#Checkov
RUN pip3 install checkov
RUN wget -O - https://deb.nodesource.com/setup_current.x | bash
RUN apt-get install -y nodejs
#Gradle
RUN wget https://services.gradle.org/distributions/gradle-7.0.1-bin.zip -P /tmp
RUN unzip -d /opt/gradle /tmp/gradle-7.0.1-bin.zip
RUN ln -s /opt/gradle/gradle-7.0.1 /opt/gradle/latest
ENV PATH="/opt/gradle/latest/bin:${PATH}"
#RUN chmod +x /etc/profile.d/gradle.sh
#RUN source /etc/profile.d/gradle.sh
#copy certificates
COPY ./utils/ca_tp_pem.crt /root/
COPY ./utils/ca_tp_pem_sha256.crt /root/
COPY ./utils/rootca_pem.crt /root

#Add certs to cacerts
RUN keytool -importcert -file /root/ca_tp_pem.crt -keystore /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/cacerts  -alias "catp" -storepass changeit -noprompt
RUN keytool -importcert -file /root/ca_tp_pem_sha256.crt -keystore /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/cacerts  -alias "catp256" -storepass changeit -noprompt
RUN keytool -importcert -file /root/rootca_pem.crt -keystore /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/cacerts  -alias "rootsignet" -storepass changeit -noprompt


ENV SDK_URL="https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip"
ENV ANDROID_HOME="/usr/local/android-sdk"
ENV ANDROID_VERSION=29
ENV ANDROID_BUILD_TOOLS_VERSION=30.0.2
RUN apt install curl -y
# Download Android SDK
RUN mkdir "$ANDROID_HOME"
RUN cd "$ANDROID_HOME" && curl -o sdk.zip $SDK_URL && unzip sdk.zip && rm sdk.zip
RUN ls -la $ANDROID_HOME
RUN mkdir "$ANDROID_HOME/licenses" || true
RUN echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" > "$ANDROID_HOME/licenses/android-sdk-license"
#    && yes | $ANDROID_HOME/tools/bin/sdkmanager --licenses
# Install Android Build Tool and Libraries
RUN mkdir /root/.android
RUN touch /root/.android/repositories.cfg
RUN ls -la $ANDROID_HOME
RUN $ANDROID_HOME/tools/bin/sdkmanager --update
RUN $ANDROID_HOME/tools/bin/sdkmanager "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" \
    "platforms;android-${ANDROID_VERSION}" \
    "platform-tools"
# Install Build Essentials
RUN apt-get update && apt-get install build-essential file apt-utils -y


#user
MAINTAINER siewrgrz
RUN adduser mixeway
#RUN mkdir /.npm
#RUN chown -R 1001:1001 "/.npm"
RUN mkdir /opt/sources
RUN chown mixeway /opt/sources
RUN chown mixeway /bin/gitleaks
RUN chown -R mixeway:mixeway /tmp/kics
#USER mixeway

# Building Mixeway Scanner APP
WORKDIR /app
#COPY --chown=mixeway ./target/mixewaytesting-1.0.1-SNAPSHOT.jar ./app.jar
COPY --from=maven /app/app.jar ./app.jar

ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "/app/app.jar"]