#!/bin/bash
# Give execute permission to mvnw
chmod +x mvnw

# Run Maven build with skipTests
./mvnw clean install -DskipTests
