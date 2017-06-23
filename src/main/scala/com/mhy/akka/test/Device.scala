package com.mhy.akka.test

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, Props}
import com.mhy.akka.test.Device.{ReadTemprature, RespondTemperature}

/**
  * Created by mhy on 2017/6/23.
  */
object Device{
  def props(groupId :String,deviceId : String) : Props = Props(new Device(groupId,deviceId))

  final case class ReadTemprature(requestId : Long)
  final case class RespondTemperature(requestId : Long,value:Option[Double])
}

class Device(groupId:String,deviceId : String) extends Actor with ActorLogging{
  var lastTemperatureReading : Option[Double] = None

  override def preStart(): Unit = log.info("Device actor {}-{} started", groupId, deviceId)

  override def postStop(): Unit = log.info("Device actor {}-{} stoped", groupId, deviceId)

  override def receive: Receive = {
    case ReadTemprature(id) =>
      sender() ! RespondTemperature(id, lastTemperatureReading)
  }
}

