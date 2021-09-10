package clicker.game

import akka.actor.{Actor, ActorRef}
import clicker._
import play.api.libs.json.{JsValue, Json, JsString}



class GameActor(username: String, var configuration: String) extends Actor {

  // --------------------- The Big Parse ~ using a method to parse the data from Json--------------------------

  def BigParse(configuration: String): Map[String, Equipment] = {

    var retMap: Map[String,Equipment] = Map()

    val parse: JsValue = Json.parse(configuration)
    val currency: String = (parse \ "currency").as[String]

    val equipmentIter: List[JsValue] = (parse \ "equipment").as[List[JsValue]]
    for (i <- 0 to equipmentIter.length-1){

      val id: String = (equipmentIter(i) \ "id").as[String]
      val name: String = (equipmentIter(i) \ "name").as[String]
      val incomePerClick: Int = (equipmentIter(i) \ "incomePerClick").as[Int]
      val incomePerSecond: Int = (equipmentIter(i) \ "incomePerSecond").as[Int]
      val initialCost: Int = (equipmentIter(i) \ "initialCost").as[Int]
      val priceExponent: Double = (equipmentIter(i) \ "priceExponent").as[Double]
     // println("id:"+id + " name:" +name+" incomePerClick:"+incomePerClick+" incomePerSecond:"+incomePerSecond+" initialCost:"+initialCost+ " priceExponet:"+priceExponent)
      val equipmentGenerated: Equipment = new Equipment(id,name,incomePerClick,incomePerSecond,initialCost,priceExponent)
      retMap = retMap + (id -> equipmentGenerated)
    }
    retMap
  }

  // ------------------------ State Variables-------------------------------------

  // Username stored - thank yourself
  val user: String = username

  // Keep track of the currency in the game actor class
  var currencyAmount: Double = 0.0

  // Data structure storing the equipments(values) to their id's(keys) after parsing the configuration
  // each equipment has an id stored as a stat variable for ease of use
  val equipment: Map[String, Equipment] = BigParse(configuration)

  // The json equipment storage
  var equipmentJson: List[JsValue] = null

  // println("Printed Equipment Map " +this.equipment)
  // Set time of the last update (starting when the game is created) update with time when update message is recieved
  var time: Long = System.nanoTime()


// ----------------------------  Click message ----------------------------------
    def increaseGold(): Unit = {
      var increaseCurrency: Int = 1
      val keyList: List[String] = this.equipment.keySet.toList
      val lenKeyList: Int = keyList.length
      for (i <- 0 to lenKeyList-1){
        val currentID: String = keyList(i)
        val currentEquipment: Equipment = this.equipment.getOrElse(currentID, null)
        increaseCurrency += currentEquipment.incomePerClick()
       // println(increaseCurrency)
      }
      val newCurrency: Double = this.currencyAmount + increaseCurrency
      this.currencyAmount = newCurrency
   //   println("Gold per Click Generated " +newCurrency)
   //  println("Time Stamp: " +this.time)
  }

  // ---------------------------- BuyEquipment message --------------------------

  def affordable(equipment: Equipment): Boolean = {
    var bool: Boolean = false
    if (equipment.Price() <= this.currencyAmount){
      bool = true
    }
    bool
  }

  def buyEquipment(equipment: String): Unit = {

    val Equ: Equipment = this.equipment.getOrElse(equipment, null)
    if (affordable(Equ)){
     this.currencyAmount =  this.currencyAmount - Equ.Price()
      Equ.Owned += 1
    }
  }

  // ----------------------------  Update Message -------------------------------

  def returnedJsonTry1(): String = {
    val equipmentKeys: List[String] = this.equipment.keySet.toList
    var newEquipmentList: List[String] = List()
    for (i <- 0 to equipmentKeys.length -1) {
      val currentEqu: Equipment = this.equipment.getOrElse(equipmentKeys(i), null)
      val Jsid: JsValue = Json.toJson(currentEqu.ID)
      val JsnumberOwned: JsValue = Json.toJson(currentEqu.Owned)
      val Jscost: JsValue = Json.toJson(currentEqu.Price())
      val Iteration: Map[String,JsValue] = Map(
        "id" -> Jsid,
        "numberOwned" -> JsnumberOwned,
        "cost" -> Jscost
      )
        newEquipmentList  = Json.stringify(Json.toJson(Iteration)) :: newEquipmentList
    }
    val JsUser: JsValue = Json.toJson(this.user)
    println("currency when Json String is formed "+this.currencyAmount+ " at timeStamp: "+ this.time)
    val JsCurrency: JsValue = Json.toJson(this.currencyAmount)
    println("Print statment in the ReturnedJson method, printing it's current gold amount: " +this.currencyAmount)
    val JsNewEquipmentList: JsValue = Json.toJson(newEquipmentList.reverse)
    val JsEndList: Map[String, JsValue] = Map(
      "username" -> JsUser,
      "currency" -> JsCurrency,
      "equipment" -> JsNewEquipmentList)
   val endList: String =  Json.stringify(Json.toJson(JsEndList))
    println("-------------------this is the Json string returned------------------")
    println(endList)
    endList
  }

  def returnedJson(): String = {

    val equipmentKeysLis: List[String] = this.equipment.keySet.toList
    val JsonArray: Array[JsValue] = (for (i <- 0 to equipmentKeysLis.length -1) yield {
      val currentEqu: Equipment = this.equipment.getOrElse(equipmentKeysLis(i), null)
      val jsonObj: JsValue = Json.obj(
        "id" -> currentEqu.ID,
        "numberOwned" -> currentEqu.Owned,
        "cost" -> currentEqu.Price()
      )
      jsonObj
    }).toArray
    val json: JsValue = Json.obj(
      "username" -> this.user,
      "currency" -> this.currencyAmount,
      "equipment" -> JsonArray)
  val Jso0n:String =   Json.stringify(json)
    println(Jso0n)
    Jso0n
  }



  def passiveCurrency(): Unit = {
    val currentTime: Long = System.nanoTime()
    val time: Long = (currentTime - this.time)/1000000000
    println("This is the time since last update in seconds: "+time)
    val equipmentKeys: List[String] = this.equipment.keySet.toList
    var addedCurrency: Double = 0.0
    this.time = currentTime
    for (i <- 0 to equipmentKeys.length -1){
      val currentEqu: Equipment = this.equipment.getOrElse(equipmentKeys(i),null)
      addedCurrency = addedCurrency + (currentEqu.incomePerSec() * time)
    }
    println("Passive Gold Generated "+ addedCurrency)
    this.currencyAmount =  this.currencyAmount + addedCurrency
  }

  def updateGame(sender:ActorRef): Unit = {
    passiveCurrency()
    sender ! GameState(returnedJson())
  }

  override def receive: Receive = {
    case Click =>  increaseGold()
    case BuyEquipment(equipment: String) => buyEquipment(equipment)
    case Update => updateGame(sender())

  }

}
