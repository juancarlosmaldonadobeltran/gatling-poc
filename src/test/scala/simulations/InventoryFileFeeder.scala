package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt
import scala.util.Random

/*
  Running a simulation:

  mvn gatling:test \
  -Dgatling.simulationClass=simulations.InventoryFileFeeder \
  -DPROTOCOL=http \
  -DBASE_URL=http://localhost:8080 \
  -DFILE_PATH=/home/juan/workspace/gatling-poc/src/main/resources/feeders/outputURLLoad.csv \
  -DREQUESTS_PER_SECOND=2 \
  -DDURATION_IN_SECONDS=60

 */
class InventoryFileFeeder extends Simulation {

  /** * Variables ** */
  // runtime variables

  def baseURL: String = getProperty("BASE_URL", "http://localhost:8080")

  /**
   * Allowed values: http or http2
   */
  def protocol: String = getProperty("PROTOCOL", "http")

  def filePath: String = getProperty("FILE_PATH", "/")

  def requestsPerSecond: Int = getProperty("REQUESTS_PER_SECOND", "5").toInt

  def durationInSeconds: Int = getProperty("DURATION_IN_SECONDS", "20").toInt

  val csvFeeder = csv(filePath).circular

  val rnd = new Random()

  /** * Helper Methods ** */
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  val http1Conf = http.baseUrl(baseURL)
    .header("Accept", "application/json")
//    .proxy(Proxy("localhost", 8888))

  val http2Conf = http1Conf.enableHttp2

  val httpConf = protocol match {
    case "http" => http1Conf
    case "http2" => http2Conf
  }

  def getInventory() = {
    repeat(durationInSeconds) {
      feed(csvFeeder)
        .exec(http("Get Inventory")
          .get("${req}")
          .check(status.is(200)))
        .pause(1 second)
    }
  }

  /** * Scenario Design ** */
  val scn = scenario("Projections API")
    .exec(getInventory())


  /** * Setup Load Simulation ** */
  setUp(
    scn.inject(atOnceUsers(requestsPerSecond))
  ).protocols(httpConf)

  /** * Before ** */
  before {
    println(s"Running the test")
    println(s"PROTOCOL: ${protocol}")
    println(s"BASE_URL: ${baseURL}")
    println(s"FILE_PATH: ${filePath}")
    println(s"REQUESTS_PER_SECOND: ${requestsPerSecond}")
    println(s"DURATION_IN_SECONDS: ${durationInSeconds}")
  }

  /** * After ** */
  after {
    println("Stress test completed")
  }

}
