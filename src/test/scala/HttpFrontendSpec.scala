import java.util.UUID

import akka.actor.Actor
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import akka.util.Timeout
import model.Comment
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by diemyst on 24.09.15.
 */
class HttpFrontendSpec extends WordSpec with Matchers with BeforeAndAfterAll with ScalaFutures
                                with ScalatestRouteTest with HttpFrontend with Defaults.connector.Connector {

  override var login: String = "test"
  override var password: String = "test"

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.result(db.autocreate().future(), 5.seconds)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    //Await.result(db.autotruncate().future(), 5.seconds)
  }

  implicit val testSystem =
    akka.actor.ActorSystem("test-system")
  implicit val fm = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(60.seconds)

  def testRoute(request: HttpRequest): Unit = {
    request ~> route ~> check {
      status shouldBe OK
      println(responseAs[String])
    }
  }

  "asd" in {
    testRoute(Get("/"))
    Put("/post", model.Post(None, "hello", "text", None, Some(DateTime.now()))) ~> route ~> check {
      status shouldBe OK
      println(responseAs[String])
    }
    testRoute(Get("/"))
    testRoute(Get("/post/" + new UUID(13l, 29l)))
    testRoute(Patch("/post/" + new UUID(13l, 29l), model.Post(Some(new UUID(13l, 29l)), "hello", "text", None, None)))
    testRoute(Delete("/post/" + new UUID(13l, 29l)))
    testRoute(Put("/post/" + new UUID(13l, 29l) + "/comment", Comment(None, new UUID(13l, 29l), "hello comment", "guest1", None)))
    testRoute(Delete("/post/" + new UUID(13l, 29l) + "/comment/" + new UUID(13l, 29l)))

  }

  "Save post in cassandra" in {
    val uuid: UUID = db.posts.store(model.Post(None, "title", "text", None, None))
    val postFut = db.posts.getById(uuid)
    val post = Await.result(postFut, 5.seconds)
    post.get.title shouldBe "title"
  }
}

class TestActor extends Actor {
  override def receive: Receive = {
    case _@m => println(m)
  }
}
