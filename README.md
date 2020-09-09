[![](https://api.travis-ci.org/juancarlosmaldonadobeltran/gatling-poc.svg)](https://travis-ci.com/github/juancarlosmaldonadobeltran/gatling-poc)
# Gatling PoC

Gatling scenario PoC

## Prerequisites
* Java SE 8
* Maven

## Running a simulation

`mvn gatling:test -Dgatling.simulationClass=simulations.PetStore -DBASE_URL=http://localhost:8080/projections_war/ -DRMS_SKU_ID=89134036 -DLOCATION_ID=320 -DUSERS=10 -DRAMP_DURATION=5 -DDURATION=20`



