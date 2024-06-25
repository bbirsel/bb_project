//import SpotifyApiClient.system
import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import java.util.Properties
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger


object Main extends App{
  // config
  val conf = ConfigFactory.load
  val clientId = Try(conf.getString("app.crypto.client-id")) match {
    case Success(s) => s
    case Failure(e) =>
      logger.error("required parameter uri is missing")
      throw Exception(e)
  }
  val clientSecret = Try(conf.getString("app.crypto.client-secret")).toOption

  val uri = Try(conf.getString("app.crypto.uri")).toOption

  // AKKA
  // entry point for Akka and is used to create and manage actors
  implicit val system: ActorSystem = ActorSystem()
  // allocate the resources needed to run a stream
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // for executing asynchronous operations, fetching the access token
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher


  // spotify client id and secret
  // to access the authentication code
 /* val clientId = "99198d9242074487b1de6c4a4354bf53"
  val clientSecret = "ba42a630685e4e58b2db170483868c68"

  val uri = "https://api.spotify.com/v1/playlists/7HV1vvwRIt7jki0mjnEt9H"*/

  // kafka initialization
  val props = new Properties()
  // properties for Kafka producer are set
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

  // yeni kafka producer yarat
  val producer = new KafkaProducer[String, String](props)
  val topic = "SpotifyAPITopic"

  // Once you have the access token, you can use it to call the method from SpotifyApiClient
  // hepsinin aynı akka system üstünden çalışması için parameter verdim
  SpotifyAuthClient.getAccessToken(system, materializer, executionContext,clientId.get, clientSecret.get).onComplete {
    case Success(token) => val playlistsResponse = SpotifyApiClient.getMyPlaylist(token, uri.get)
      playlistsResponse.onComplete {
          case Success(response) =>
            println(s"Fetched: $response")
            // kafka producer
            val record = new ProducerRecord[String, String](topic, "key", response.toString)
            //The send() method is asynchronous. When called it adds the record to a buffer of pending
            // record sends and immediately returns.
            producer.send(record)

          case Failure(exception) =>
            println(s"Failed to get new releases: $exception")
        }
      }


  // shutdown hook to terminate the system when the application exits
  scala.sys.addShutdownHook {
    producer.close()
    system.terminate()
  }
}