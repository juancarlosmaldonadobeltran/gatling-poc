[![](https://api.travis-ci.org/juancarlosmaldonadobeltran/gatling-poc.svg)](https://travis-ci.com/github/juancarlosmaldonadobeltran/gatling-poc)
# Gatling PoC

Gatling scenario PoC

## Prerequisites
* Java SE 8
* Maven

## Running a simulation

`mvn gatling:test -Dgatling.simulationClass=simulations.PetStore -DUSERS=10 -DRAMP_DURATION=5 -DDURATION=20`



