import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

/**
 * Created by diemyst on 24.09.15.
 */
object Appl extends App with HttpFrontend {

  override var login: String = "username"
  override var password: String = "password"

  if (args.isEmpty) {
    println("test credentials")
    println("login: username")
    println("password: password")
  } else {
    login = args(0)
    password = args(1)
  }

  override implicit val system = ActorSystem("test-system", ConfigFactory.load())
  override implicit val timeout: Timeout = Timeout(60.seconds)
  implicit val materializer = ActorMaterializer()

  Http(system).bindAndHandle(route, host, port)

}
