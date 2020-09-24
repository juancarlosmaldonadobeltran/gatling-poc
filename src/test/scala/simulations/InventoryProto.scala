package simulations

import io.gatling.core.Predef._
import io.gatling.core.session.SessionAttribute
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import io.gatling.http.response.Response
import io.gatling.http.response.StringResponseBody

import scala.concurrent.duration.DurationInt
import scala.util.Random

class InventoryProto extends Simulation {

  /** * Variables ** */
  // runtime variables
  def baseURL: String = getProperty("BASE_URL", "https://localhost:8443/canonical/")

  def pathURL: String = getProperty("PATH_URL", s"inventory")

  def rmsSkuIds: String = getProperty("RMS_SKU_IDS", "94785796,12798663").split(",").filter(_.nonEmpty).map("rmsSkuId=" + _).mkString("&")

  def locationIds: String = getProperty("LOCATION_IDS", "").split(",").filter(_.nonEmpty).map("locationId=" + _).mkString("&")

  def query: String = s"?${if(rmsSkuIds.isEmpty) "" else rmsSkuIds}${if(locationIds.isEmpty) "" else s"&${locationIds}"}"

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
//    .acceptHeader("application/x-protobuf")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .header("Content-Type", "application/force-download")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:53.0) Gecko/20100101 Firefox/53.0")
//    .proxy(Proxy("localhost", 8888))


  def getInventory() = {
    exec(
      http("Get inventory")
        .get(path)
        .check(status.is(200))
//        .check(bodyString.saveAs("responseBody"))
        .check(responseTimeInMillis.lt(100))
    )
//    exec {
//      session =>
//        val responseBody: SessionAttribute = session("bodyStream")
//        println(responseBody.as[String]); session
//    }
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
