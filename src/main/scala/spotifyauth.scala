import AkkaHttpClient.{producer, topic}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContextExecutor, Future}

object SpotifyAuthClient {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  case class AccessToken(access_token: String, token_type: String, expires_in: Int)

  val clientId = "0601d2709b8444d3bd669a0a052e4bca"
  val clientSecret = "97efc1dc3931457caa40e25fdc33bced"

  def getAccessToken(): Future[String] = {
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
    val entityFuture = responseFuture.flatMap(response => response.entity.toStrict(5.seconds))
    entityFuture.map(entity => entity.data.utf8String)

      /*.foreach {
      { element =>
        println(element)
        element
          convertTo[ApiResponse] match {
          case apiResponse: ApiResponse =>
            // kafka producer
            val record = new ProducerRecord[String, String](topic, "key", apiResponse.url)
            //The send() method is asynchronous. When called it adds the record to a buffer of pending
            // record sends and immediately returns.
            producer.send(record)
              .to[AccessToken]
        }
        .map(_.access_token)
      }*/
    }
  }
