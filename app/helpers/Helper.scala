package helpers

import actors.UpdatingActor
import actors.UpdatingActor.Update
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Helper {
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val mat: ActorMaterializer = ActorMaterializer()

  val updater: ActorRef = actorSystem.actorOf(Props(new UpdatingActor))
  actorSystem.scheduler.schedule(0.second, 1.hour, updater, Update)
}
