
import Main.{producer, topic}
import akka.actor.{ActorSystem, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import org.apache.kafka.clients.producer.ProducerRecord
import spray.json._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}






// JSON handling support
// how to serialize and deserialize JSON data (converting between JSON and Scala objects)
trait JsonSupport2 extends DefaultJsonProtocol {
  implicit val authResponseFormat: RootJsonFormat[APIResponse] = jsonFormat1(APIResponse)
}

// Case class to represent API response
case class APIResponse(access_token: String)

object SpotifyAuthClient extends App with JsonSupport2{

  //case class AccessToken(access_token: String, token_type: String, expires_in: Int)

  def getAccessToken(implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContextExecutor, clientId: String, clientSecret:String): Future[String] = {
  //def getAccessToken(): Future[String] = {

    // used to pass authentication information from the client to the server
    val authHeader = headers.Authorization(BasicHttpCredentials(clientId, clientSecret))
    // create the body of the HTTP POST request that will be sent to Spotify's token
    // endpoint to request an access token
    val entity = FormData("grant_type" -> "client_credentials").toEntity

    // POST request made to Spotify's token endpoint

    val responseFuture = Http().singleRequest(
      HttpRequest(
        method = HttpMethods.POST,
        uri = "https://accounts.spotify.com/api/token",
        headers = List(authHeader),
        entity = entity
      )
    )

   /* responseFuture.onComplete {
      case Success(token) => val jsonString = token.toString()
        print(jsonString)
        // JSON string from the response is parsed into a JSON object
        val jsonParsed = jsonString.parseJson
        println(jsonParsed)
        // extract access token
        jsonParsed.convertTo[APIResponse].access_token


    }
  }*/

    // single request methods

    // ensure asynchronous processing with flatmap
    responseFuture.flatMap { response =>
      // strict entity: HTTP entity that has been fully materialized or loaded into memory,
      // allowing you to work with it as a complete object.
      response.entity.toStrict(5.seconds).flatMap { strictEntity =>
        val jsonString = strictEntity.data.utf8String
        print(jsonString)
        // JSON string from the response is parsed into a JSON object
        val jsonParsed = jsonString.parseJson
        println(jsonParsed)
        // extract access token
        val accessToken = jsonParsed.convertTo[APIResponse].access_token
        // Return the access token as a successful future
        Future.successful(accessToken)
      }
    }
  }

/*
  // Handle the future and print the access token
  getAccessToken.onComplete {
    case Success(token) => println(s"Access token: $token")
    case Failure(exception) => println(s"Failed to get access token: $exception")
  }

  // Ensure the application runs long enough for the future to complete
  Thread.sleep(10000)

  // Terminate the actor system
  system.terminate()*/
  }


