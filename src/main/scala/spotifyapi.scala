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
/*trait JsonSupport extends DefaultJsonProtocol {
  implicit val authResponseFormat: RootJsonFormat[AuthResponse] = jsonFormat1(AuthResponse)
}

// Case class to represent API response
case class AuthResponse(name: String)*/
// JSON handling support
trait JsonSupport extends DefaultJsonProtocol {
  implicit val artistFormat: RootJsonFormat[Artist] = jsonFormat2(Artist)
  implicit val albumFormat: RootJsonFormat[Album] = jsonFormat2(Album)
  implicit val trackFormat: RootJsonFormat[Track] = jsonFormat1(Track)
  implicit val addedByFormat: RootJsonFormat[AddedBy] = jsonFormat1(AddedBy)
  implicit val itemFormat: RootJsonFormat[Item] = jsonFormat2(Item)
  implicit val tracksFormat: RootJsonFormat[Tracks] = jsonFormat1(Tracks)
  implicit val playlistFormat: RootJsonFormat[Playlist] = jsonFormat3(Playlist)
}

case class Artist(name: String, id: String)
case class Album(name: String, artists: List[Artist])
case class Track(name: String)
case class AddedBy(id: String)
case class Item(track: Track, added_by: AddedBy)
case class Tracks(items: List[Item])
case class Playlist(name: String, owner: AddedBy, tracks: Tracks)



object SpotifyApiClient extends App with JsonSupport {



  def getMyPlaylist(accessToken: String, uri: String)(implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContextExecutor): Future[List[String]] = {
    val authHeader = headers.Authorization(OAuth2BearerToken(accessToken))
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = uri,
      headers = List(authHeader)
    )

    val responseFuture = Http().singleRequest(request)

    responseFuture.flatMap { response =>
      response.entity.toStrict(5.seconds).flatMap { strictEntity =>
        val jsonString = strictEntity.data.utf8String
        //print(jsonString)
        val jsonParsed = jsonString.parseJson
        //println(s"json parsed: $jsonParsed")
        val playlist = jsonParsed.convertTo[Playlist]
        val songNames = playlist.tracks.items.map(_.track.name)
        Future.successful(songNames)
      }
    }

  }
  //getNewReleases("BQDx4xjDL77jLWDmsvnuLywB3hm-mVk5VXThzXX-W3mdLPsRoVmdFR9XdxDoX2CvUMqgGJY2QOf6jGyzPsDKD-AuMfBFSKSBraXuF0A67aa0q-UeGR8")

}