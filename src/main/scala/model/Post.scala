package model

import java.util.UUID

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.column.DateTimeColumn
import com.websudos.phantom.dsl._
import com.websudos.phantom.connectors.RootConnector
import com.websudos.phantom.keys.PartitionKey
import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * Created by diemyst on 24.09.15.
 */
case class Post(id: Option[UUID], title: String, text: String, created: Option[DateTime], updated: Option[DateTime])

class Posts extends CassandraTable[ConcretePosts, Post] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object text extends StringColumn(this)
  object title extends StringColumn(this)
  object created extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Descending
  object updated extends OptionalDateTimeColumn(this)

  def fromRow(row: Row): Post = {
    Post (
      Some(id(row)),
      title(row),
      text(row),
      Some(created(row)),
      updated(row)
    )
  }
}

abstract class ConcretePosts extends Posts with RootConnector {

  def store(post: Post): UUID = {
    //не очень красиво генерить UUID здесь, но и разделять модель с case class'ом для json очень оверхедно
    val uuid = post.id.getOrElse(UUID.randomUUID())
    insert.value(_.id, uuid).value(_.text, post.text)
      .value(_.title, post.title)
      .value(_.created, post.created.getOrElse(DateTime.now()))
      .value(_.updated, post.updated)
      .future()
    //по идее тут должна быть обработка ошибок, но я не нашел решение, как из ResultSet достать информацию нужную
    uuid
  }

  def deletePost(id: UUID): Future[Unit] = {
    delete.where(_.id eqs id).future().map(_ => {})
  }

  def getById(id: UUID): Future[Option[Post]] = {
    select.where(_.id eqs id).one()
  }

  def getPostsPage(start: UUID, limit: Int): Future[List[Post]] = {
    select.where(_.id gtToken start).limit(limit).fetch()
  }

  def getFirstPage(limit: Int): Future[List[Post]] = {
    select.limit(limit).fetch()
  }

  def updatePost(id: UUID, post: Post): Future[ResultSet] = {
    update.where(_.id eqs id)
      .modify(_.title setTo post.title)
      .and(_.text setTo post.text)
      .and(_.updated setTo Some(DateTime.now()))
      .future()
  }
}
