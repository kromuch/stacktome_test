package controllers

import actors.UpdatingActor.GetCNN
import akka.actor.ActorSystem
import helpers.Helper
import javax.inject._
import play.api.mvc._
import akka.pattern.ask
import akka.util.Timeout
import models.TopicTrend

import scala.concurrent.duration._

import scala.concurrent.ExecutionContext

@Singleton
class TopicsController @Inject()(cc: ControllerComponents,
                                 actorSystem: ActorSystem)
                                (implicit exec: ExecutionContext) extends AbstractController(cc) {
  implicit val timeout: Timeout = 5.seconds

  def topics: Action[AnyContent] = Action.async {
    ask(Helper.updater, GetCNN).mapTo[List[TopicTrend]].map {
      tt: List[TopicTrend] =>
        Ok(views.html.topics(tt))
    }
  }
}