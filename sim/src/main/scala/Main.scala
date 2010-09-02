import edu.stanford.junction.sample.partyware._
import scala.actors.Actor
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import scala.util.control.Breaks._
import scala.util.Random._

import edu.stanford.junction.JunctionException
import edu.stanford.junction.JunctionMaker
import edu.stanford.junction.Junction
import edu.stanford.junction.api.activity.JunctionActor
import edu.stanford.junction.api.activity.JunctionExtra
import edu.stanford.junction.api.activity.ActivityScript
import edu.stanford.junction.api.messaging.MessageHeader
import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig
import org.json.JSONObject
import java.util.ArrayList
import java.util.Random
import java.util.Date
import java.net._

case class Wakeup()

import Data._

object Main {

  val users: ListBuffer[User] = ListBuffer()

  def main(args: Array[String]) {
    for (i <- 1 until 10) {
      newUser(i)
    }
  }

  def newUser(i: Int) {
    val u = new User(i)
    u.start
  }

}

class User(i: Int) extends Actor {

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

  val prop = new PartyProp("party_prop")
  val jxActor = new JunctionActor("participant") {
    override def onActivityJoin() {
      println("User " + i + " joined activity!")
    }
    override def onActivityCreate() {
      println("User " + i + " created activity!")
    }
    override def onMessageReceived(header: MessageHeader, msg: JSONObject) {
      println("User " + i + " got message!")
    }

    override def getInitialExtras(): java.util.List[JunctionExtra] = {
      val l = new ArrayList[JunctionExtra]()
      l.add(prop)
      l
    }
  }

  val host = "openjunction.org"

  def initJunction {

    var url: URI = null;
    try {
      url = new URI("junction://" + host + "/partyware")
      try {
        val jxMaker = JunctionMaker.getInstance(new XMPPSwitchboardConfig(host))
        val jx: Junction = jxMaker.newJunction(url, jxActor)
      } catch {
        case e: JunctionException => {
          println("Failed to connect to junction activity!")
          e.printStackTrace(System.err)
        }
        case e: Exception => {
          println("Failed to connect to junction activity!")
          e.printStackTrace(System.err)
        }
      }

    } catch {
      case e: URISyntaxException => {
        println("Failed to parse uri.")
      }
    }
  }

  val name = randomName + i
  val thumb = randomPortrait
  val id = "user" + i

  def act() {
    initJunction
    val rng = new Random()
    ActorPing.scheduleAtFixedRate(this, Wakeup(), rng.nextInt(1000), 1000)
    loop {
      try {
        react {
          case Wakeup() => {
            doWithProb((0.05, { () =>
              prop.addImage(id, randomImage, randomImage, "...", (new Date()).getTime() / 1000)
            }), (0.03, { () =>
              val vid = randomVideo
              val thumb = "http://img.youtube.com/vi/" + vid + "/default.jpg";
              prop.addYoutube(id, vid, thumb, "...", (new Date()).getTime() / 1000)
            }), (0.03, { () =>
              val id = randomVideoInProp(prop).optString("id")
              prop.upvoteVideo(id)
            }), (0.03, { () =>
              val id = randomVideoInProp(prop).optString("id")
              prop.downvoteVideo(id)
            }), (0.03, { () =>
              prop.updateUser(id, name, "", thumb)
            }), (0.03, { () =>
	      val toId = randomUserInProp(prop).optString("id")
              prop.addRelationship(
		relations.toArray, 
		reverseRelations.toArray, 
		id, toId, randomRelation)
            }))
          }
          case msg => {
            println("Unexpected msg: " + msg)
          }
        }
      } catch {
        case e: Exception => {
          println("Error in msg loop, " + e)
        }
      }
    }
  }
}
