#!/bin/bash

# ✅ Fix permission locally
chmod +x mvnw build.sh

# 🧹 Remove from git index without deleting locally (so we can re-add with correct permissions)
git rm --cached mvnw
git rm --cached build.sh

# ✅ Re-add with executable bit tracked
git add mvnw build.sh

# ✅ Commit with message
git commit -m "fix: ensure mvnw and build.sh are executable"

# ✅ Push to GitHub
git push

# 🏃‍♂️ Now, you can run the build command while skipping tests
./mvnw clean package -DskipTests
