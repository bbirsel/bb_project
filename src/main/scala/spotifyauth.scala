
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


  case class AccessToken(access_token: String, token_type: String, expires_in: Int)



  def getAccessToken(implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContextExecutor, clientId: String, clientSecret:String): Future[String] = {
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


    responseFuture.flatMap { response =>
      response.entity.toStrict(5.seconds).flatMap { strictEntity =>
        val jsonString = strictEntity.data.utf8String
        print(jsonString)
        val jsonParsed = jsonString.parseJson
        println(jsonParsed)
        val accessToken = jsonParsed.convertTo[APIResponse].access_token
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


