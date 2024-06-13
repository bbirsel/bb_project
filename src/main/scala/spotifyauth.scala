
import akka.actor.{ActorSystem, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import org.apache.kafka.clients.producer.ProducerRecord
import spray.json._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


// JSON handling support
// how to serialize and deserialize JSON data (converting between JSON and Scala objects)
trait JsonSupport extends DefaultJsonProtocol {
  implicit val authResponseFormat: RootJsonFormat[AuthResponse] = jsonFormat1(AuthResponse)
}

// Case class to represent API response
case class AuthResponse(access_token: String)

object SpotifyAuthClient extends App with JsonSupport{

  /*implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher*/

  case class AccessToken(access_token: String, token_type: String, expires_in: Int)

  val clientId = "0601d2709b8444d3bd669a0a052e4bca"
  val clientSecret = "97efc1dc3931457caa40e25fdc33bced"

  def getAccessToken(implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContextExecutor): Future[String] = {
  //def getAccessToken(): Future[String] = {

    val authHeader = headers.Authorization(BasicHttpCredentials(clientId, clientSecret))
    val entity = FormData("grant_type" -> "client_credentials").toEntity

    val responseFuture = Http().singleRequest(
      HttpRequest(
        method = HttpMethods.POST,
        uri = "https://accounts.spotify.com/api/token",
        headers = List(authHeader),
        entity = entity
      )
    )
    println(responseFuture)
    println("y")
    /*
    responseFuture.onComplete {
      case Success(response) =>  response.entity.toStrict(5.seconds).onComplete {
        case Success(strictEntity) =>
          val jsonString = strictEntity.data.utf8String
          println(s"JSON response: $jsonString")
          val jsonParsed = jsonString.parseJson
          println(s"Parsed JSON response: $jsonParsed")
          val accessToken = jsonParsed.convertTo[AuthResponse].access_token
          accessToken
      }}*/

    responseFuture.flatMap { response =>
      response.entity.toStrict(5.seconds).flatMap { strictEntity =>
        val jsonString = strictEntity.data.utf8String
        print(jsonString)
        val jsonParsed = jsonString.parseJson
        println(jsonParsed)
        val accessToken = jsonParsed.convertTo[AuthResponse].access_token
        Future.successful(accessToken)
      }
    }


    }
  /*// Handle the future and print the access token
  getAccessToken.onComplete {
    case Success(token) => println(s"Access token: $token")
    case Failure(exception) => println(s"Failed to get access token: $exception")
  }

  // Ensure the application runs long enough for the future to complete
  Thread.sleep(10000)

  // Terminate the actor system
  system.terminate()
  }*/}
