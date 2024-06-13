import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._

import scala.concurrent.{ExecutionContextExecutor, Future}
import SpotifyAuthClient.{getAccessToken, system}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer


object SpotifyApiClient extends App {
/*  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher*/


  def getNewReleases(accessToken: String)(implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContextExecutor): Future[HttpResponse] = {
    val authHeader = headers.Authorization(OAuth2BearerToken(accessToken))
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = "https://api.spotify.com/v1/browse/new-releases",
      headers = List(authHeader)
    )

    Http().singleRequest(request)

  }
  //getNewReleases("BQDx4xjDL77jLWDmsvnuLywB3hm-mVk5VXThzXX-W3mdLPsRoVmdFR9XdxDoX2CvUMqgGJY2QOf6jGyzPsDKD-AuMfBFSKSBraXuF0A67aa0q-UeGR8")

}