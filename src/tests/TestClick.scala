package tests

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clicker._
import clicker.game.GameActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._
import scala.io.Source


class TestClick extends TestKit(ActorSystem("TestGame"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  val EPSILON: Double = 0.000001

  def equalDoubles(d1: Double, d2: Double): Boolean = {
    (d1 - d2).abs < EPSILON
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A Clicker actor" must {

    "react to clicks and equipment purchases" in {

      val configuration: String = Source.fromFile("goldConfig.json").mkString
      val gameActor = system.actorOf(Props(classOf[GameActor], "test", configuration))

      gameActor ! Click

      expectNoMessage(20000.millis)

      gameActor ! Update
      val state: GameState = expectMsgType[GameState](9000.millis)
      val jsonState = state.gameState
      val gameState: JsValue = Json.parse(jsonState)
      val gold = (gameState \ "currency").as[Double]
      val expectedGold = 1.0
      print("returned Gold " + gold )
      assert(equalDoubles(gold, expectedGold))

    }

    "TestCase 1" in {

      val configuration: String = Source.fromFile("goldConfig.json").mkString
      val gameActor = system.actorOf(Props(classOf[GameActor], "test", configuration))

      var i = 0

      while (i < 200) {
        gameActor ! Click

        expectNoMessage(20.millis)
        i +=1
      }

        gameActor ! Update
        val state: GameState = expectMsgType[GameState](1000.millis)

      val jsonState = state.gameState
      val gameState: JsValue = Json.parse(jsonState)
      val gold = (gameState \ "currency").as[Double]
      val expectedGold = 200.0
      assert(equalDoubles(gold, expectedGold))
    }

    "TestCase 2" in{
      val configuration: String = Source.fromFile("goldConfig.json").mkString
      val gameActor = system.actorOf(Props(classOf[GameActor], "test", configuration))

      gameActor ! BuyEquipment("shovel")
      expectNoMessage(20.millis)

      gameActor ! BuyEquipment("excavator")
      expectNoMessage(200.millis)

      gameActor ! BuyEquipment("mine")
      expectNoMessage(200.millis)

      gameActor ! Click

      expectNoMessage(20000.millis)

      gameActor ! Update
      val state: GameState = expectMsgType[GameState](9000.millis)
      val jsonState = state.gameState
      val gameState: JsValue = Json.parse(jsonState)
      val gold = (gameState \ "currency").as[Double]
      val expectedGold = 1.0
      print("returned Gold " + gold )
      assert(equalDoubles(gold, expectedGold))
    }

    "TestCase 3" in{
      val configuration: String = Source.fromFile("goldConfig.json").mkString
      val gameActor = system.actorOf(Props(classOf[GameActor], "test", configuration))

      var i = 0

      while (i < 200) {
        gameActor ! Click

        expectNoMessage(20.millis)
        i +=1
      }
      gameActor ! Update
      val state3: GameState = expectMsgType[GameState](1000.millis)

      val jsonState3 = state3.gameState
      val gameState3: JsValue = Json.parse(jsonState3)
      val gold3:Double = (gameState3 \ "currency").as[Double]
      val expectedGold3 = 188.475
      println("Gold after 200 clicks before the purchase of 3 shovels")
      println("Expected Gold Generated Gold: "+gold3)


      gameActor ! BuyEquipment("shovel")
      expectNoMessage(200.millis)

      gameActor ! BuyEquipment("shovel")
      expectNoMessage(200.millis)

      gameActor ! BuyEquipment("shovel")
      expectNoMessage(200.millis)

      gameActor ! Update
      val state2: GameState = expectMsgType[GameState](1000.millis)


      val jsonState2 = state2.gameState
      val gameState2: JsValue = Json.parse(jsonState2)
      val gold2 = (gameState2 \ "currency").as[Double]
      println("Currency after the shovel purchase: "+gold2)
      val equipmentLis2: List[String] = (gameState2 \ "equipment").as[List[String]]
      println(equipmentLis2)

      var j: Int = 0
      while (j < 5) {
        gameActor ! Click

        expectNoMessage(20.millis)
        j +=1
      }
      gameActor ! Update
      val state: GameState = expectMsgType[GameState](1000.millis)

      val jsonState = state.gameState
      val gameState: JsValue = Json.parse(jsonState)
      val gold = (gameState \ "currency").as[Double]
      val expectedGold = 188.475
      val equipmentLis: List[String] = (gameState \ "equipment").as[List[String]]
      println(equipmentLis)
      println("Currency after 5 clicks with the shovels")
      println("Expected Gold: "+expectedGold+" Generated Gold: "+gold)
      assert(equalDoubles(gold, expectedGold))

//      val equipment = (gameState \ "equipment").as[List[String]]
//      val shovel = equipment(equipment.indexOf("shovel"))
//      val
    }
  }
}
