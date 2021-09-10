package tests

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clicker._
import clicker.game.GameActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._
import scala.io.Source

class IdleTesting extends TestKit(ActorSystem("TestGame"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  // error margin
  val EPSILON: Double = 0.3

  def equalDoubles(given: Double, expected: Double): Boolean = {
    var ret: Boolean  = false
    val lowMargin: Double = expected*(1 - EPSILON)
    val upperMargin: Double = expected*(1+EPSILON)
    if(lowMargin < given && given < upperMargin){
      ret = true
    }
    ret
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }
"Idle testing" must{
  "Test Case 1" in{
    val configuration: String = Source.fromFile("goldConfig.json").mkString
    val gameActor = system.actorOf(Props(classOf[GameActor], "test", configuration))
    var i = 0

    while (i < 200) {
      gameActor ! Click

      expectNoMessage(20.millis)
      i +=1
    }
    gameActor ! Update

    gameActor ! BuyEquipment("excavator")

    val timeAfterPurchase: GameState = expectMsgType[GameState](200.millis)

    expectNoMessage(980.millis)

    gameActor ! Update
    val state: GameState = expectMsgType[GameState](1000.millis)
    val jsonState = state.gameState
    val gameState: JsValue = Json.parse(jsonState)
    val gold = (gameState \ "currency").as[Double]
    val expected: Double = 10.0
    println(gameState)
    println("This is the given gold amount: "+gold+ " this is the expected: "+expected)
    assert(equalDoubles(gold,expected))

  }
  "Test case 2"in{
    val configuration: String = Source.fromFile("goldConfig.json").mkString
    val gameActor = system.actorOf(Props(classOf[GameActor], "test", configuration))

    var i = 0
    while (i < 200) {
      gameActor ! Click

      expectNoMessage(20.millis)
      i +=1
    }

    gameActor ! BuyEquipment("excavator")
    expectNoMessage(200.millis)

    gameActor ! Update
    val state2: GameState = expectMsgType[GameState](1000.millis)

    var j: Int = 0
    while (j < 100) {
      gameActor ! Click

      expectNoMessage(20.millis)
      j +=1
    }
    gameActor ! BuyEquipment("mine")


    expectNoMessage(1000.millis)
    gameActor ! Update

    val state: GameState = expectMsgType[GameState](1000.millis)
    val jsonState = state.gameState
    val gameState: JsValue = Json.parse(jsonState)
    val gold = (gameState \ "currency").as[Double]
    val expected: Double = 100.0
    println(gameState)
    println("This is the given gold amount: "+gold+ " this is the expected: "+expected)
    assert(equalDoubles(gold,expected))
  }
}
}
