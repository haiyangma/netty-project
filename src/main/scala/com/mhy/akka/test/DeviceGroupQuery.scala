package com.mhy.akka.test

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import com.mhy.akka.test.DeviceGroup._

import scala.concurrent.duration.FiniteDuration

/**
  * Created by mhy on 2017/6/27.
  * http://doc.akka.io/docs/akka/current/scala/guide/tutorial_4.html
  */
class DeviceGroupQuery(
    actorToDeviceId : Map[ActorRef,String],
    requestId : Long,
    requester : ActorRef,
    timeout:FiniteDuration) extends Actor with ActorLogging{

  import DeviceGroupQuery._
  import context.dispatcher

  val queryTimeoutTimer = context.system.scheduler.scheduleOnce(timeout,self,CollectionTimeout)

  override def preStart(): Unit = {
      actorToDeviceId.keysIterator.foreach(deviceActor =>{
        context.watch(deviceActor)
        deviceActor ! ReadTemperature(0)
      }
    )
  }

  override def postStop(): Unit = {
    queryTimeoutTimer.cancel()
  }

  override def receive: Receive = {
    waitingForReplies(
      Map.empty,
      actorToDeviceId.keySet
    )
  }

  def waitingForReplies(
   repliseSoFar : Map[String,DeviceGroup.TemperatureReading],
   stillWatting:Set[ActorRef]
   ):Receive={
    case RespondTemperature(0,valueOption) =>
      val deviceActor = sender()
      val reading = valueOption match {
        case Some(value) => Temperature(value)
        case None => TemperatureNotAvailable
      }
      receivedResponse(deviceActor,reading,stillWatting,repliseSoFar)

    case Terminated(deviceActor) =>
      receivedResponse(deviceActor,DeviceNotAvailable,stillWatting,repliseSoFar)

    case CollectionTimeout =>
      val timeOutReplise =
        stillWatting.map{ deviceActor =>
          val deviceId = actorToDeviceId(deviceActor)
          deviceId -> DeviceTimedOut
        }
      requester ! RespondAllTemperatures(requestId,repliseSoFar ++ timeOutReplise)
      context.stop(self)
  }

  def receivedResponse(
                        deviceActor:  ActorRef,
                        reading:      DeviceGroup.TemperatureReading,
                        stillWaiting: Set[ActorRef],
                        repliesSoFar: Map[String, DeviceGroup.TemperatureReading]
                      ): Unit = {
    context.unwatch(deviceActor)
    val deviceId = actorToDeviceId(deviceActor)
    val newStillWaiting = stillWaiting - deviceActor

    val newRepliesSoFar = repliesSoFar + (deviceId -> reading)
    if (newStillWaiting.isEmpty) {
      requester ! DeviceGroup.RespondAllTemperatures(requestId, newRepliesSoFar)
      context.stop(self)
    } else {
      context.become(waitingForReplies(newRepliesSoFar, newStillWaiting))
    }
  }
}

object DeviceGroupQuery{
  case object CollectionTimeout

  def props(
     actorToDeviceId : Map[ActorRef,String],
     requestId:Long,
     requester : ActorRef,
     timeout: FiniteDuration
     ):Props={
      Props(new DeviceGroupQuery(actorToDeviceId,requestId,requester,timeout))
  }
}
