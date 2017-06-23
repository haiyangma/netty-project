package com.mhy.akka.test
import akka.actor.ActorSystem

import scala.io.StdIn
/**
  * Created by mhy on 2017/6/23.
  */
class IotApp {

}

object IotApp extends App{
  val system = ActorSystem("iot-system")
  val supervisor = system.actorOf(IotSupervisor.props)
  StdIn.readLine()
}
