FROM ubuntu:18.04

MAINTAINER Yannic Noller <yannic.noller@acm.org>

# Dependencies
RUN apt-get -y update
RUN apt-get -y install git build-essential openjdk-8-jdk wget unzip ant python3 python3-numpy vim nano
RUN update-java-alternatives --set /usr/lib/jvm/java-1.8.0-openjdk-amd64
RUN wget https://services.gradle.org/distributions/gradle-4.4.1-bin.zip -P /tmp
RUN unzip -d /opt/gradle /tmp/gradle-*.zip
ENV GRADLE_HOME=/opt/gradle/gradle-4.4.1
ENV PATH=${GRADLE_HOME}/bin:${PATH}

# Installing HyDiff
WORKDIR /root
RUN git clone https://github.com/yannicnoller/hydiff.git --branch v1.0.0
WORKDIR /root/hydiff
RUN ./setup.sh
