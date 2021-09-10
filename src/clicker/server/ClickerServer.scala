package clicker.server

import akka.actor.{Actor, ActorSystem, Props, ActorRef}
import clicker._
import com.corundumstudio.socketio.listener.DataListener
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import play.api.libs.json.{JsValue, Json}
import clicker.game.GameActor

import scala.io.Source



// Impemented as both a websocket server and an actor system
class ClickerServer(val configuration: String) extends Actor {


  //Data structure for username, socket, and GameActor Storage
  var usernameToSocket: Map[String, SocketIOClient] = Map()
  var socketToUsername: Map[SocketIOClient, String] = Map()
  var usernameToGameActor: Map[String, ActorRef] = Map()
  var GameActorToSocket: Map[ActorRef,SocketIOClient] = Map()
  var socketToGameActor: Map[SocketIOClient, ActorRef] = Map()
  var allGameActors: List[ActorRef] = List()


  // Web Socket server Section

  val config: Configuration = new Configuration {
    setHostname("localhost")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)
  server.addEventListener("startGame", classOf[String], new startGame(this))
  server.addEventListener("click", classOf[Nothing], new click(this))
  server.addEventListener("buy", classOf[String], new buy(this))
  server.start()

  def manageUpdates(): Unit = {
    for (i <- 0 to this.allGameActors.length - 1) {
      val curActor: ActorRef = this.allGameActors(i)

      curActor ! Update
    }
  }


  def manageGame(sender: ActorRef, gameState: String): Unit = {
    val curSocket: SocketIOClient = this.GameActorToSocket.getOrElse(sender,null)
    curSocket.sendEvent("gameState", gameState)
  }

  // Actor Message section
  override def receive: Receive = {
    // send the game state of to the webSocket  associated with this player
    case GameState(gameState: String) => manageGame(sender(), gameState)

    //When the Update games message is received send an Update message to all the game actors
    case UpdateGames => manageUpdates()

  }
}
  class startGame(server: ClickerServer) extends DataListener[String] {

    def repetitiveUser(server: ClickerServer, username: String): Boolean = {
      if (server.usernameToSocket.contains(username)) {
        true
      }
      else {
        false
      }
    }
    override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {

      //1.) Check to see if the username already exists ~ repetitive user method
      if(repetitiveUser(server, username)) {
        //1c.) If True ~ Return the original socket and calculate the idle income and send the
      }else {
        //1c.) If false ~ Store the username, socket, and GameActor data
        val currentGameActor: ActorRef = server.context.actorOf(Props(classOf[GameActor],username, this.server.configuration))
        this.server.usernameToSocket += (username -> socket)
        this.server.allGameActors = currentGameActor :: this.server.allGameActors
        this.server.socketToUsername += (socket -> username)
        this.server.socketToGameActor += (socket -> currentGameActor)
        this.server.usernameToGameActor += (username -> currentGameActor)
        this.server.GameActorToSocket += (currentGameActor -> socket)

        //4.) Send message of type "initialize" containing the new configuration to the sender of the startGame message
        socket.sendEvent("initialize",this.server.configuration)
      }
    }
  }

  class click(server: ClickerServer) extends DataListener[Nothing] {
    override def onData(socket: SocketIOClient, nonData: Nothing, ackRequest: AckRequest): Unit = {

      //1.) Look up the players GameActor from their webSocket

      val currentGameActor: ActorRef = this.server.socketToGameActor.getOrElse(socket,null)

      //2.) Send a click message to the GameActor associated with the player
     currentGameActor ! Click
    }
  }

  class buy(server: ClickerServer) extends DataListener[String] {
    override def onData(socket: SocketIOClient, equipmentID: String, ackRequest: AckRequest): Unit = {

      //1.) look up the players GameActor from their webSocket

      val currentGameActor: ActorRef = this.server.socketToGameActor.getOrElse(socket,null)

      //2.) Send a BuyEquipment message to the GameActor associated with the webSocket
      // The message includes the equipment ID
      print("buy Registered ClickerSever")
      currentGameActor ! BuyEquipment(equipmentID)
    }


    def postStop(): Unit = {
      println("stopping server")
       //server.stop()
    }
  }

  object ClickerServer {
    def main(args: Array[String]): Unit = {

      val actorSystem = ActorSystem()

      import actorSystem.dispatcher

      import scala.concurrent.duration._

      val configuration: String = Source.fromFile("goldConfig.json").mkString

      val server = actorSystem.actorOf(Props(classOf[ClickerServer], configuration))

      actorSystem.scheduler.schedule(0.milliseconds, 100.milliseconds, server, UpdateGames)
    }
  }



