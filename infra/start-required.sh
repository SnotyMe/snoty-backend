#!/bin/bash

for file in */docker-compose.yml; do
	docker compose -f "$file" up -d
done
