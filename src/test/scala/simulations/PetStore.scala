package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt
import scala.util.Random

class PetStore extends Simulation {

  /** * Variables ** */
  // runtime variables
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

  val httpConf = http.baseUrl("https://petstore.swagger.io/v2/")
    .header("Accept", "application/json")
//    .proxy(Proxy("localhost", 8888))

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  /** * Custom Feeder ** */
  val customFeeder = Iterator.continually(Map(
    "name" -> ("MyPet-" + randomString(5))
  ))

  def createPet() = {
    feed(customFeeder).
      exec(http("Create a new Pet")
        .post("pet")
        .body(ElFileBody("bodies/NewPetTemplate.json")).asJson //template file goes in gating/resources/bodies
        .check(status.is(200))
        .check(bodyString.saveAs("responseBody")))
      .exec { session => println(session("responseBody").as[String]); session }
  }

  /** * Scenario Design ** */
  val scn = scenario("Pet Store API")
    .forever() {
      exec(createPet())
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
