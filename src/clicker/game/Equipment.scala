package clicker.game

class Equipment(id:String,name: String, incomePerClick: Int, incomePerSec: Int, initialCost: Int, priceExponent: Double) {

  val ID: String = id
  val Name: String = name
  var Owned: Int = 0

  def incomePerClick(): Int = {
    this.Owned * incomePerClick
  }
  def incomePerSec(): Int = {
    this.Owned * incomePerSec
  }
  def Price(): Double = {
   val multi: Double = math.pow(priceExponent, this.Owned)
    (multi * initialCost)
  }
}
