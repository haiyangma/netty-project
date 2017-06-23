package com.mhy.akka.test

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, Props}

/**
  * Created by mhy on 2017/6/23.
  */
final case class ReadTemperature(requestId : Long)
final case class RespondTemperature(requestId : Long,value:Option[Double])
object Device{
  def props(groupId :String,deviceId : String) : Props = Props(new Device(groupId,deviceId))
}

class Device(groupId:String,deviceId : String) extends Actor with ActorLogging{
  var lastTemperatureReading : Option[Double] = None

  override def preStart(): Unit = log.info("Device actor {}-{} started", groupId, deviceId)

  override def postStop(): Unit = log.info("Device actor {}-{} stoped", groupId, deviceId)

  override def receive: Receive = {
    case ReadTemperature(id) =>
      sender() ! RespondTemperature(id, lastTemperatureReading)
  }
}

