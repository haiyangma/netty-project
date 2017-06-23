import akka.actor.{Actor, ActorSystem, Props}

class PrintMyActorRefActor extends Actor {


  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {

  }

  override def receive: Receive = {
    case "printit" =>
      val secondRef = context.actorOf(Props.empty, "second-actor")
      println(s"Second: $secondRef")
  }
}
object PrintMyActorRefActor{
  def main(args: Array[String]): Unit = {
    val system=ActorSystem("UniversityMessageSystem")
    val firstRef = system.actorOf(Props[PrintMyActorRefActor], "first-actor")
    println(s"First : $firstRef")
    firstRef ! "printit"
    system.terminate();
  }
}
