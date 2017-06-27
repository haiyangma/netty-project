package com.mhy.akka.test

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, Props}

/**
  * Created by mhy on 2017/6/23.
  */
final case class RecordTemperature(requestId: Long, value: Double)
final case class TemperatureRecorded(requestId: Long)
final case class ReadTemperature(requestId : Long)
final case class RespondTemperature(requestId : Long,value:Option[Double])
final case class RequestTrackDevice(groupId: String, deviceId: String)
case object DeviceRegistered
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
    case ReadTemperature(id) =>
      sender() ! RespondTemperature(id, lastTemperatureReading)

    case RecordTemperature(id, value) =>
      log.info("Recorded temperature reading {} with {}", value, id)
      lastTemperatureReading = Some(value)
      sender() ! TemperatureRecorded(id)

    case RequestTrackDevice(`groupId`, `deviceId`) =>
      sender() ! DeviceRegistered

    case RequestTrackDevice(groupId, deviceId) =>
      log.warning(
        "Ignoring TrackDevice request for {}-{}.This actor is responsible for {}-{}.",
        groupId, deviceId, this.groupId, this.deviceId
      )
  }
}

