//import SpotifyApiClient.system
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}

import java.util.Properties
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
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
  SpotifyAuthClient.getAccessToken.onComplete {
    case Success(token) => println(s"Access token: $token")
    case Failure(exception) => println(s"Failed to get access token: $exception")
    /*val newReleasesResponse = SpotifyApiClient.getNewReleases(accessToken)
    newReleasesResponse.foreach { response =>
      // Handle the response here
      println(response)
    }*/
  }

  // Add a shutdown hook to terminate the system when the application exits
  scala.sys.addShutdownHook {
    system.terminate()
  }
}