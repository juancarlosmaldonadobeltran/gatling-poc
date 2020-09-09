package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt
import scala.util.Random

class Inventory extends Simulation {

  /** * Variables ** */
  // runtime variables
  def baseURL: String = getProperty("BASE_URL", "http://localhost:8080/projections_war/")

  def rmsSkuId: String = getProperty("RMS_SKU_ID", "89134036")

  def locationId: String = getProperty("LOCATION_ID", "320")

  def userCount: Int = getProperty("USERS", "5").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt

  def testDuration: Int = getProperty("DURATION", "30").toInt

  val rnd = new Random()

  /** * Helper Methods ** */
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  val httpConf = http.baseUrl(baseURL)
    .header("Accept", "application/json")
//    .proxy(Proxy("localhost", 8888))


  def getInventory() = {
      exec(
        http("Get inventory")
        .get(s"inventory?rmsSkuId=${rmsSkuId}&locationId=${locationId}")
        .check(status.is(200))
        .check(bodyString.saveAs("responseBody")))
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
  }

  /** * After ** */
  after {
    println("Stress test completed")
  }

}
