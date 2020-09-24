package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

/**
 * Running a simulation:
 *
 * mvn gatling:test -Dgatling.simulationClass=simulations.InventoryHTTP2  -DUSERS=1 -DRAMP_DURATION=1 -DDURATION=20
 */
class InventoryHTTP2 extends Simulation {

  /** * Variables ** */
  // runtime variables
  def baseURL: String = getProperty("BASE_URL", "https://localhost:8443")

  def path: String = s""

  def userCount: Int = getProperty("USERS", "5").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt

  def testDuration: Int = getProperty("DURATION", "30").toInt

  /** * Helper Methods ** */
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  val httpConf = http.baseUrl(baseURL)
    .header("Accept", "application/json")
    .enableHttp2
    .http2PriorKnowledge(Map("localhost:8443" -> true))

  def getInventory() = {
    exec(
      http("Get HTTP2")
        .get("")
        .check(status.is(200))
        .check(bodyString.saveAs("responseBody"))
    )
      .exec { session => println(session("responseBody").as[String]); session }
  }

  /** * Scenario Design ** */
  val scn = scenario("Projections API")
    .forever() {
      exec(getInventory())
        .pause(1)
    }

  /** * Setup Load Simulation ** */
  setUp(
    scn.inject(
      nothingFor(5 seconds),
      rampUsers(userCount) during (rampDuration seconds))
  )
    .protocols(httpConf)
    .maxDuration(testDuration seconds)

  /** * Before ** */
  before {
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
    println(s"Base URL: ${baseURL}")
    println(s"Path: ${path}")
  }

  /** * After ** */
  after {
    println("Stress test completed")
  }

}
