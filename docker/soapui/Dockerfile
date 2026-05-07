FROM eclipse-temurin:11-jre

# Install dependencies
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    bash \
    && rm -rf /var/lib/apt/lists/*

# Download and install SoapUI Open Source
ENV SOAPUI_VERSION=5.7.0
RUN wget https://s3.amazonaws.com/downloads.eviware/soapuios/${SOAPUI_VERSION}/SoapUI-${SOAPUI_VERSION}-linux-bin.tar.gz \
    && tar -xzf SoapUI-${SOAPUI_VERSION}-linux-bin.tar.gz -C /opt \
    && rm SoapUI-${SOAPUI_VERSION}-linux-bin.tar.gz \
    && ln -s /opt/SoapUI-${SOAPUI_VERSION} /opt/soapui

# Add SoapUI to PATH
ENV PATH="/opt/soapui/bin:${PATH}"

# Set working directory
WORKDIR /workspace

# Default command (testrunner.sh)
ENTRYPOINT ["/opt/soapui/bin/testrunner.sh"]
CMD ["--help"]
