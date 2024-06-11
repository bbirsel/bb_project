import akka.actor.{ActorSystem, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import java.util.Properties
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer

// JSON handling support
// how to serialize and deserialize JSON data (converting between JSON and Scala objects)
trait JsonSupport extends DefaultJsonProtocol {
  implicit val apiResponseFormat: RootJsonFormat[ApiResponse] = jsonFormat1(ApiResponse)
}

// Case class to represent API response
case class ApiResponse(url: String)

object AkkaHttpClient extends App with JsonSupport {
  implicit val system: ActorSystem = ActorSystem()
  // deprecated find what can be used instead
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val apiKey = "live_hrv56Sa4XAoSn4hoIZCVBPYZmt9LCPhEKekeWEM2VtoGgrRTcuxh4e6v9i1jmYkQ"
  val url = s"https://api.thedogapi.com/v1/images/search"
  val interval = 3.seconds

  // kafka initialization
  val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

  val producer = new KafkaProducer[String, String](props)
  val topic = "DogAPITopic"

  // Function to make the API request
  def fetchApiData(): Unit = {
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = url
    ).withHeaders(
      akka.http.scaladsl.model.headers.RawHeader("x-api-key", apiKey)
    )

    //  result of the asynchronous HTTP request
    val responseFuture: Future[HttpResponse] = Http().singleRequest(request)

    responseFuture.onComplete {
      case Success(response) =>
        response.status match {
          case StatusCodes.OK =>
            response.entity.toStrict(5.seconds).onComplete {
              case Success(strictEntity) =>
                val jsonString = strictEntity.data.utf8String
                println(s"JSON response: $jsonString")
                val jsonParsed = jsonString.parseJson
                println(s"Parsed JSON response: $jsonParsed")
                jsonParsed match {
                  case JsArray(elements) if elements.nonEmpty =>
                    elements.headOption.foreach { element =>
                      println(element)
                      element.convertTo[ApiResponse] match {
                        case apiResponse: ApiResponse =>
                          // kafka producer
                          val record = new ProducerRecord[String, String](topic, "key", apiResponse.url)
                          //The send() method is asynchronous. When called it adds the record to a buffer of pending
                          // record sends and immediately returns.
                          producer.send(record)

                        case _ => println("Unexpected JSON structure")
                      }
                    }
                  case _ => println("Unexpected JSON structure")
                }
              case Failure(ex) => println(s"Failed to convert entity to strict: $ex")
            }
          case _ =>
            println(s"Response status unexpected: ${response.status}")
        }
      case Failure(ex) =>
        println(s"Failed to fetch API data: $ex")
    }
  }

  // Schedule periodic API requests
  val cancellable: Cancellable = system.scheduler.scheduleAtFixedRate(
    initialDelay = 0.seconds,
    interval = interval
  )(() => fetchApiData())

  // Handle shutdown
  scala.sys.addShutdownHook {
    cancellable.cancel()
    producer.close()
    system.terminate()
  }
}
