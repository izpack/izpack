#!/bin/bash
set -xe

MVN_VERSION="$1"
MVN_INSTALL_DIR="$2"

if [ ! -f "${MVN_INSTALL_DIR}/lib/maven-artifact-${MVN_VERSION}.jar" ]; then
  rm -Rf "${MVN_INSTALL_DIR}"
  mkdir -p "${MVN_INSTALL_DIR}"

  APACHE_MIRROR="https://archive.apache.org/dist"
  curl -o "${HOME}/apache-maven-$MVN_VERSION-bin.tar.gz" "$APACHE_MIRROR/maven/maven-3/$MVN_VERSION/binaries/apache-maven-$MVN_VERSION-bin.tar.gz"
  cd "${MVN_INSTALL_DIR}"
  tar -xzf "${HOME}/apache-maven-$MVN_VERSION-bin.tar.gz" --strip 1
  chmod +x "${MVN_INSTALL_DIR}/bin/mvn"
else
  echo "Using cached Maven ${MVN_VERSION}"
fi
${MVN_INSTALL_DIR}/bin/mvn -version
