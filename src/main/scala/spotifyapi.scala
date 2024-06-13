import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._

import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.duration.DurationInt
import spray.json._
// JSON handling support
// how to serialize and deserialize JSON data (converting between JSON and Scala objects)
trait JsonSupport extends DefaultJsonProtocol {
  implicit val authResponseFormat: RootJsonFormat[AuthResponse] = jsonFormat1(AuthResponse)
}

// Case class to represent API response
case class AuthResponse(name: String)


object SpotifyApiClient extends App with JsonSupport {



  def getMyPlaylist(accessToken: String)(implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContextExecutor): Future[String] = {
    val authHeader = headers.Authorization(OAuth2BearerToken(accessToken))
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = "https://api.spotify.com/v1/playlists/7HV1vvwRIt7jki0mjnEt9H",
      headers = List(authHeader)
    )

    val responseFuture = Http().singleRequest(request)

    responseFuture.flatMap { response =>
      response.entity.toStrict(5.seconds).flatMap { strictEntity =>
        val jsonString = strictEntity.data.utf8String
        print(jsonString)
        val jsonParsed = jsonString.parseJson
        println(jsonParsed)
        val accessToken = jsonParsed.convertTo[AuthResponse].name
        Future.successful(accessToken)
      }
    }

  }
  //getNewReleases("BQDx4xjDL77jLWDmsvnuLywB3hm-mVk5VXThzXX-W3mdLPsRoVmdFR9XdxDoX2CvUMqgGJY2QOf6jGyzPsDKD-AuMfBFSKSBraXuF0A67aa0q-UeGR8")

}