import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import model.{Comment, Post}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import spray.json._

import scala.concurrent.{Future, Await}

/**
 * Created by diemyst on 23.09.15.
 */
case class UUIDJson(uuid: UUID)

trait Protocols extends DefaultJsonProtocol with SprayJsonSupport {

  implicit object DateJsonFormat extends RootJsonFormat[DateTime] {
    private val parserISO : DateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis()
    override def write(obj: DateTime) = JsString(parserISO.print(obj))
    override def read(json: JsValue) : DateTime = json match {
      case JsString(s) => parserISO.parseDateTime(s)
      case _ => throw new DeserializationException("Error info you want here ...")
    }
  }

  implicit object UuidJsonFormat extends JsonFormat[UUID] {
    def write(x: UUID) = JsString(x toString ())
    def read(value: JsValue) = value match {
      case JsString(x) => UUID.fromString(x)
      case x => deserializationError("Expected UUID as JsString, but got " + x)
    }
  }

  implicit def commentFormat = jsonFormat5(Comment.apply)
  implicit def postFormat = jsonFormat5(Post.apply)
  implicit def uuidFormat = jsonFormat1(UUIDJson.apply)
}

trait HttpFrontend extends Protocols {

  implicit val system: ActorSystem
  implicit val timeout: Timeout
  implicit val materializer: ActorMaterializer
  import scala.concurrent.ExecutionContext.Implicits.global

  val db: BlogiesDatabase = BlogiesDatabase
  val posts = db.posts
  val comments = db.comments

  val route: Route = {
    get {
      pathSingleSlash {
        complete(posts.getFirstPage(10))
      } ~
      path("post" / JavaUUID ~ Slash.?) { uuid =>
        complete {
          posts.getById(uuid)
        }

      }
    } ~
    put {
      (path("post") & entity(as[Post])) { post =>
        complete {
          UUIDJson(posts.store(post))
        }
      } ~
      path("post" / JavaUUID / "comment" ~ Slash.?) { id =>
        entity(as[Comment]) { comment =>
          complete {
            UUIDJson(comments.store(comment, id))
          }
        }
      }
    }~
    patch {
      path("post" / JavaUUID ~ Slash.?) { uuid =>
        entity(as[Post]) { post =>
          complete {
            posts.updatePost(uuid, post)
            StatusCodes.OK
          }
        }
      }
    }~ delete {
      path("post" / JavaUUID ~ Slash.?) { uuid =>
        complete {
          posts.deletePost(uuid)
          StatusCodes.OK
        }
      } ~
      path("post" / JavaUUID / "comment" / JavaUUID ~ Slash.?) { (postId, commentId) =>
        complete {
          comments.deleteComment(commentId)
          StatusCodes.OK
        }
      }
    }
  }


}
