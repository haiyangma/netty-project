package com.mhy.akka.test

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestActors, TestKit, TestProbe}
import com.mhy.akka.test.Device.ReadTemprature
import com.mhy.akka.test.DeviceGroup.{ReplyDeviceList, RequestDeviceList, RespondAllTemperatures, Temperature}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration.DurationInt

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

    deviceActor.tell(RecordTemperature(requestId = 1, 24.0), probe.ref)
    probe.expectMsg(TemperatureRecorded(requestId = 1))

    deviceActor.tell(ReadTemperature(requestId = 2), probe.ref)
    val response1 = probe.expectMsgType[Device.RespondTemperature]
    response1.requestId should ===(2)
    response1.value should ===(Some(24.0))

    deviceActor.tell(RecordTemperature(requestId = 3, 55.0), probe.ref)
    probe.expectMsg(TemperatureRecorded(requestId = 3))

    deviceActor.tell(ReadTemperature(requestId = 4), probe.ref)
    val response2 = probe.expectMsgType[Device.RespondTemperature]
    response2.requestId should ===(4)
    response2.value should ===(Some(55.0))

  }

  "reply to registration requests" in {
    val probe = TestProbe()
    val deviceActor = system.actorOf(Device.props("group", "device"))

    deviceActor.tell(RequestTrackDevice("group", "device"), probe.ref)
    probe.expectMsg(DeviceRegistered)
    probe.lastSender should ===(deviceActor)
  }


  "ignore wrong registration requests" in {
    val probe = TestProbe()
    val deviceActor = system.actorOf(Device.props("group", "device"))

    deviceActor.tell(RequestTrackDevice("wrongGroup", "device"), probe.ref)
    probe.expectNoMsg(500.milliseconds)

    deviceActor.tell(RequestTrackDevice("group", "Wrongdevice"), probe.ref)
    probe.expectNoMsg(500.milliseconds)
  }

  "be able to list active devices" in {
    val probe = TestProbe()
    val groupActor = system.actorOf(DeviceGroup.props("group"))

    groupActor.tell(RequestTrackDevice("group","device1"),probe.ref)
    probe.expectMsg(DeviceRegistered)

    groupActor.tell(RequestTrackDevice("group","device2"),probe.ref)
    probe.expectMsg(DeviceRegistered)

    groupActor.tell(RequestDeviceList(requestId = 0),probe.ref)
    probe.expectMsg(ReplyDeviceList(requestId = 0,Set("device1","device2")))

  }

  "be able to list active devices after one shuts down" in {
    val probe = TestProbe()
    val groupActor = system.actorOf(DeviceGroup.props("group"))

    groupActor.tell(RequestTrackDevice("group","device1"),probe.ref)
    probe.expectMsg(DeviceRegistered)
    val shutDown = probe.lastSender

    groupActor.tell(RequestTrackDevice("group","device2"),probe.ref)
    probe.expectMsg(DeviceRegistered)

    groupActor.tell(RequestDeviceList(requestId = 0),probe.ref)
    probe.expectMsg(ReplyDeviceList(requestId = 0,Set("device1","device2")))

    shutDown ! PoisonPill
    probe.expectTerminated(shutDown,500.second)

    probe.awaitAssert{
      groupActor.tell(RequestDeviceList(requestId = 1),probe.ref)
      probe.expectMsg(ReplyDeviceList(requestId = 1,Set("device2")))
    }
  }

  "return temperature value for working devices" in {
    val requester = TestProbe()

    val device1 = TestProbe()
    val device2 = TestProbe()

    val queryActor = system.actorOf(DeviceGroupQuery.props(
      actorToDeviceId = Map(device1.ref -> "device1",device2.ref -> "device2"),
      requestId = 1,
      requester = requester.ref,
      timeout = 3.seconds
    ))

    device1.expectMsg(ReadTemprature(requestId = 0))
    device2.expectMsg(ReadTemprature(requestId = 0))

    queryActor.tell(RespondTemperature(requestId = 0,Some(1.0)),device1.ref)
    queryActor.tell(RespondTemperature(requestId = 0,Some(2.0)),device2.ref)

    requester.expectMsg(RespondAllTemperatures(
      requestId = 1,
      temperatures = Map(
        "device1" -> Temperature(1.0),
        "device2" -> Temperature(2.0)
      )
    ))

  }

}


