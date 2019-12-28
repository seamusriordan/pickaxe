#!/bin/bash
cd client
yarn install
yarn build
cd ../server
./gradlew wrapper
cd ..
docker build . -t pickaxe
