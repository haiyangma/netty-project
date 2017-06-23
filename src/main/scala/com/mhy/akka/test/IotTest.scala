package com.mhy.akka.test

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActors, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by mhy on 2017/6/23.
  */
class IotTest extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
{
  "reply with empty reading if no temperature is known" in {
    val probe = TestProbe()
    val deviceActor = system.actorOf(Device.props("group", "device"))
    deviceActor.tell(ReadTemperature(requestId = 42), probe.ref)
    val response = probe.expectMsgType[RespondTemperature]
    response.requestId should ===(42)
    response.value should ===(None)
  }
}


