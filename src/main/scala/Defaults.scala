import com.websudos.phantom.connectors.{KeySpaceDef, ContactPoints}
import com.websudos.phantom.db.DatabaseImpl
import model.{ConcreteComments, ConcretePosts}

/**
 * Created by diemyst on 24.09.15.
 */
object Defaults {

  val hosts = Seq("127.0.0.1")


  val connector = ContactPoints(hosts).keySpace("blogies")

}

class BlogiesDatabase(val keyspace: KeySpaceDef) extends DatabaseImpl(keyspace) {
  object posts extends ConcretePosts with keyspace.Connector
  object comments extends ConcreteComments with keyspace.Connector
}

object BlogiesDatabase extends BlogiesDatabase(Defaults.connector)
