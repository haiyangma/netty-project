package com.mhy.netty.net

import grizzled.slf4j.Logger

/**
  * Created by root on 16-8-8.
  */
class TestMain {

}
object TestMain{
  val logger = Logger(classOf[TestMain])
  def main(args: Array[String]) {

    val pattern = """([0-9]{3})""".r
    for(matching <- pattern.findAllIn("123123aaa 2323bbb")){
      print(matching+"\n")
    }

  }
}