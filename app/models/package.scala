package object models {

  case class Trend(title: String) {
    override def toString: String = title
  }

  case class Topic(title: String, description: String, link: String, pubDate: String) {
    override def toString: String =
      s"$title\t$description\t$link\t$pubDate"
  }

  case class TopicTrend(title: String, description: String, link: String, pubDate: String, trend: Trend) {
    override def toString: String =
      s"$title\t$description\t$link\t$pubDate\t$trend"
  }

}
