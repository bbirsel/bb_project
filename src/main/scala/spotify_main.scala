//import SpotifyApiClient.system
import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import java.util.Properties
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}



object Main extends App{
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val clientId = "99198d9242074487b1de6c4a4354bf53"
  val clientSecret = "ba42a630685e4e58b2db170483868c68"
  // Call the method to get the access token from SpotifyAuthClient
  /*val accessTokenFuture = SpotifyAuthClient.getAccessToken.onComplete {
      case Success(token) => println(s"Access token: $token")
      case Failure(exception) => println(s"Failed to get access token: $exception")
    }*/
  //println(accessTokenFuture)

  // kafka initialization
  val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

  val producer = new KafkaProducer[String, String](props)
  val topic = "SpotifyAPITopic"

  // Once you have the access token, you can use it to call the method from SpotifyApiClient
  SpotifyAuthClient.getAccessToken(system, materializer, executionContext,clientId, clientSecret).onComplete {
    case Success(token) => val newReleasesResponse = SpotifyApiClient.getMyPlaylist(token)
      newReleasesResponse.onComplete {
          case Success(response) =>
            println(s"Fetched: $response")
            // kafka producer
            val record = new ProducerRecord[String, String](topic, "key", response)
            //The send() method is asynchronous. When called it adds the record to a buffer of pending
            // record sends and immediately returns.
            producer.send(record)

          case Failure(exception) =>
            println(s"Failed to get new releases: $exception")
        }
      }





    //
    /*
    }*/


  // Add a shutdown hook to terminate the system when the application exits
  scala.sys.addShutdownHook {
    system.terminate()
  }
}