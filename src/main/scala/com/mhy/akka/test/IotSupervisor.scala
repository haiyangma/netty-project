package com.mhy.akka.test

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, Props}

/**
  * Created by mhy on 2017/6/23.
  */
class IotSupervisor extends Actor with ActorLogging{
  override def preStart():Unit = log.info("IoT Application started")
  override def postStop(): Unit = log.info("IoT Application stopped")

  override def receive: Receive = Actor.emptyBehavior
}

object IotSupervisor{
  def props : Props = Props(new IotSupervisor)
}
