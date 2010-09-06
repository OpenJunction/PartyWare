import scala.util.Random._
import org.json.JSONObject
import scala.collection.JavaConversions._
import scala.util.control.Breaks._
import java.util.Random

object Helpers{

  def doWithProb(actions: (Double, () => Unit)*) {
    val rng = new Random()
    val roll = rng.nextDouble()
    var cur = 0.0
    breakable {
      for (action <- actions) {
        action match {
          case (prob, todo) => {
            if (roll >= cur && roll < (cur + prob)) {
              todo()
              break
            } else {
              cur += prob
            }
          }
        }
      }
    }
  }

}
