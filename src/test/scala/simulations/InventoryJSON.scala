package simulations

import com.fasterxml.jackson.databind.util.JSONPObject
import com.google.gson.JsonArray
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt
import scala.util.Random

/**
 * Running a simulation:
 *
 * mvn gatling:test -Dgatling.simulationClass=simulations.InventoryJSON -DBASE_URL=http://localhost:8080/projections/ -DPATH_URL=inventory -DRMS_SKU_IDS=94785796 -DUSERS=20 -DRAMP_DURATION=10 -DDURATION=30
 */
class InventoryJSON extends Simulation {

  /** * Variables ** */
  // runtime variables
  def baseURL: String = getProperty("BASE_URL", "http://localhost:8080/projections/")

  def pathURL: String = getProperty("PATH_URL", s"inventory")

  def rmsSkuIds: String = getProperty("RMS_SKU_IDS", "94785796").split(",").filter(_.nonEmpty).map("rmsSkuId=" + _).mkString("&")

  def locationIds: String = getProperty("LOCATION_IDS", "").split(",").filter(_.nonEmpty).map("locationId=" + _).mkString("&")

  def query: String = s"?${if (rmsSkuIds.isEmpty) "" else rmsSkuIds}${if (locationIds.isEmpty) "" else s"&${locationIds}"}"

  def path: String = s"${pathURL}${query}"

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
    .inferHtmlResources()
    .header("Accept", "application/json")
  //    .proxy(Proxy("localhost", 8888))


  def getInventory() = {
    exec(
      http("Get inventory")
        .get(path)
        .check(status.is(200))
        .check(bodyString.saveAs("responseBody"))
        .check(responseTimeInMillis.lt(100)))
      .exec { session =>
        val t0 = System.currentTimeMillis()
        import com.google.gson.Gson
        import com.google.gson.JsonObject
        val jsonArray = new Gson().fromJson(session("responseBody").as[String], classOf[JsonArray])
        val t1 = System.currentTimeMillis()
        val parsingTime = t1 - t0
        println(s"Parsing time: $parsingTime")
        session
      }
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
    println(s"Path URL: ${pathURL}")
    println(s"Path: ${path}")
    println(s"Query: ${query}")
  }

  /** * After ** */
  after {
    println("Stress test completed")
  }

}
