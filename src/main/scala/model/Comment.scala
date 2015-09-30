package model

import java.util.UUID

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.column.DateTimeColumn
import com.websudos.phantom.connectors.RootConnector
import com.websudos.phantom.dsl._
import com.websudos.phantom.keys.PartitionKey
import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * Created by diemyst on 24.09.15.
 */
case class Comment(id: Option[UUID], postId: UUID, text: String, nickname: String = "Guest", created: Option[DateTime])

class Comments extends CassandraTable[ConcreteComments, Comment] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object post_id extends UUIDColumn(this) with PrimaryKey[UUID]
  object text extends StringColumn(this)
  object nickname extends StringColumn(this)
  object created extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Descending

  def fromRow(row: Row): Comment = {
    Comment (
      Some(id(row)),
      post_id(row),
      text(row),
      nickname(row),
      Some(created(row))
    )
  }

}

abstract class ConcreteComments extends Comments with RootConnector {

  def store(comment: Comment, postId: UUID): UUID = {
    val uuid = comment.id.getOrElse(UUID.randomUUID())
    insert.value(_.id, uuid).value(_.text, comment.text)
      .value(_.post_id, postId)
      .value(_.nickname, comment.nickname)
      .value(_.created, comment.created.getOrElse(DateTime.now()))
      .future()
    uuid
  }

  def deleteComment(id: UUID): Future[ResultSet] = {
    delete.where(_.id eqs id).future()
  }

  def getCommentsByPostId(postId: UUID): Future[List[Comment]] = {
    select.where(_.post_id eqs postId).limit(10).fetch()
  }

  def getCommentsPage(start: UUID, postId: UUID, limit: Int): Future[List[Comment]] = {
    select.where(_.post_id eqs postId).and(_.id gtToken start).limit(limit).fetch()
  }
}
