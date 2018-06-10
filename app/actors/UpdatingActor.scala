package actors

import models._
import akka.actor.Actor
import com.typesafe.config.{Config, ConfigFactory}
import play.api.Logger
import UpdatingActor._

import scala.util.Try

class UpdatingActor extends Actor {
  override def receive: Receive = handler(List.empty[Trend], List.empty[TopicTrend])

  private def handler(trends: List[Trend], topics: List[TopicTrend]): Receive = {
    case Update => self ! UpdateTrends
    case UpdateTrends =>
      context.become(handler(updateTrends(trendsURL, trends), topics))
      self ! UpdateCNN
    case UpdateCNN =>
      val newTopics = updateTopics(feeds, trends)
      context.become(handler(trends, newTopics))
      Logger.info(s"${newTopics.size} trend topics parsed")
    case GetCNN => sender() ! topics
    case GetTrends => sender() ! trends
    case _ => Logger.warn("Unknown message received")
  }
}

object UpdatingActor {
  val config: Config = ConfigFactory.load()
  val trendsURL: String = config.getString("trendsRSS")
  val feeds: List[String] = scala.io.Source.fromFile(config.getString("CNN_feeds")).getLines().toList

  case object Update

  case object UpdateTrends

  case object GetTrends

  case object UpdateCNN

  case object GetCNN

  private def updateTopics(feeds: List[String], trends: List[Trend]) =
    feeds.flatMap { feedUrl =>
      Try {
        val xml = scala.xml.XML.load(new java.net.URL(feedUrl))
        val items = xml \ "channel" \ "item"
        val infoList = items.map { item =>
          val titleTemp = (item \ "title").head.text
          val description = (item \ "description").headOption.map(_.text).getOrElse("").takeWhile(_ != '<')
          //Sometimes there is no title, only description.
          val title = if (titleTemp.nonEmpty) titleTemp else if (description.nonEmpty)
            description
          else "No both title and description"
          val link = (item \ "link").head.text
          val pubDate = (item \ "pubDate").headOption.map(_.text).getOrElse("Not a piece of news")
          Topic(title, description, link, pubDate)
        }
        Logger.info(s"${infoList.size} CNN titles parsed from $feedUrl")
        infoList.toList
      }.getOrElse(List.empty[Topic])
    }.flatMap(topic => checkTrends(topic, trends))

  private def updateTrends(trendsURL: String, currentTrends: List[Trend]): List[Trend] = {
    Try {
      val xml = scala.xml.XML.load(new java.net.URL(trendsURL))
      val trendsTitles = (xml \ "channel" \ "item" \ "title").map(title => Trend(title.text))
      Logger.info(s"${trendsTitles.size} trends parsed")
      trendsTitles.toList
    }.getOrElse({
      Logger.warn("Trends updating error, old applied")
      currentTrends
    })
  }

  private def checkTrends(topic: Topic, trends: List[Trend]) = {
    val regex = "(-|,|\\+|—|;|!|\\?|%|\"|`|'|\\$|:|\\*|\\(|\\)|=|\\/|\\\\|\\||«|»|„|“|\\.|@|#|\\^|&|_|~)"
    val titleWords = topic.title.replaceAll(
      regex,
      " ")
      .split(" ")
      .map(_.trim.toLowerCase)
      .filter(_.nonEmpty)
    val descriptionWords = topic.description.replaceAll(
      regex,
      " ")
      .split(" ")
      .map(_.trim.toLowerCase)
      .filter(_.nonEmpty)
    val topicWords = (titleWords ++ descriptionWords).toSet
    val maybeTrend = trends.find { trend =>
      val trendWords = trend.title.replaceAll(
        regex,
        " ")
        .split(" ")
        .map(_.trim.toLowerCase)
        .filter(_.nonEmpty)
      val trendWordsAmount = trendWords.length
      (trendWords.toSet intersect topicWords).size == trendWordsAmount
    }
    maybeTrend match {
      case Some(trend) =>
        Some(TopicTrend(topic.title, topic.description, topic.link, topic.pubDate, trend))
      case None => None
    }
  }
}
