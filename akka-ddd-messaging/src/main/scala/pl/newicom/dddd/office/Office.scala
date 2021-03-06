package pl.newicom.dddd.office

import akka.actor.{ActorPath, ActorRef}
import akka.util.Timeout
import pl.newicom.dddd.aggregate.{EntityId, Query}
import pl.newicom.dddd.delivery.protocol.DeliveryHandler

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

object LocalOfficeId {

  implicit def fromRemoteId[E : ClassTag](remoteId: RemoteOfficeId[_]): LocalOfficeId[E] =
    LocalOfficeId[E](remoteId.id, remoteId.department)
}

case class LocalOfficeId[E : ClassTag](id: EntityId, department: String) extends OfficeId {

  def caseClass: Class[E] =
    implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]]

  def caseName: String = caseClass.getSimpleName
}

class Office(val officeId: OfficeId, val actor: ActorRef) {
  def id: EntityId = officeId.id
  def department: String = officeId.department
  def actorPath: ActorPath = actor.path

  def deliver(msg: Any)(implicit dh: DeliveryHandler): Unit = {
    dh((actorPath, msg))
  }

  def !!(msg: Any)(implicit dh: DeliveryHandler): Unit = deliver(msg)

  def ?(query: Query)(implicit ex: ExecutionContext, t: Timeout, ct: ClassTag[query.R]): Future[query.R] = {
    import akka.pattern.ask
    (actor ? query).mapTo[query.R]
  }
}