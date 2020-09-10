[![](https://api.travis-ci.org/juancarlosmaldonadobeltran/gatling-poc.svg)](https://travis-ci.com/github/juancarlosmaldonadobeltran/gatling-poc)
# Gatling PoC

Gatling scenario PoC

## Prerequisites
* Java SE 8
* Maven

## Running a simulation

`mvn gatling:test -Dgatling.simulationClass=simulations.Inventory -DBASE_URL=http://localhost:8080/projections/ -DRMS_SKU_IDS=89134036,89134036 -DLOCATION_IDS=320,330 -DUSERS=20 -DRAMP_DURATION=10 -DDURATION=30`



