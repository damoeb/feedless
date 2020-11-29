#!/usr/bin/env bash

wget https://repo1.maven.org/maven2/io/swagger/codegen/v3/swagger-codegen-cli/3.0.23/swagger-codegen-cli-3.0.23.jar -O swagger-codegen-cli.jar

java -jar swagger-codegen-cli.jar generate \
	  -i src/main/resources/swagger.yml \
	  --api-package org.migor.rss.rich.api \
	  --model-package org.migor.rss.rich.model \
	  --invoker-package org.migor.rss.rich.invoker \
	  --group-id org.migor \
	  -l kotlin-server
