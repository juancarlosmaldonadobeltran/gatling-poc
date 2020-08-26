# Gatling PoC

Gatling scenario PoC

## Prerequisites
* Java SE 8
* Maven

# Running the simulation

mvn gatling:test -Dgatling.simulationClass=simulations.PetStore -DUSERS=10 -DRAMP_DURATION=5 -DDURATION=20



