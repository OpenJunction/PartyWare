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
import java.util.UUID
import java.net._

case class Wakeup()

import Data._
import Helpers._

object Main {

  def main(args: Array[String]) {
    val monitor = new SimMonitor()
    monitor.start
  }
}

class SimMonitor() extends Actor {
  val users: ListBuffer[User] = ListBuffer()
  def act() {
    val rng = new Random()
    ActorPing.scheduleAtFixedRate(this, Wakeup(), 100, 1000)
    loop {
      try {
        react {
          case Wakeup() => {
            doWithProb((0.15, { () =>
              val id = UUID.randomUUID.toString()
              val u = new User(id)
              users += u
              u.start
            }), (0.04, { () =>
              val randUser = shuffle(users).headOption
              for (u <- randUser) {
                u ! 'kill
		users.remove(u)
              }
            }))
            println("------------")
            println("Status: " + users.length + " users.")
            println("------------")
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

class User(id: String) extends Actor {

  val prop = new PartyProp("party_prop")
  val jxActor = new JunctionActor("participant") {
    override def onActivityJoin() {
      println("--------------------------")
      println("User " + id + " joined activity!")
      println("--------------------------")
    }
    override def onActivityCreate() {
      println("User " + id + " created activity!")
    }
    override def onMessageReceived(header: MessageHeader, msg: JSONObject) {
      println("User " + id + " got message!")
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
      url = new URI("junction://" + host + "/partyware_session")
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

  val name = randomName + id
  val thumb = randomPortrait

  def act() {
    initJunction
    prop.updateUser(id, name, "", thumb)
    val rng = new Random()
    ActorPing.scheduleAtFixedRate(this, Wakeup(), rng.nextInt(1000), 1000)
    loop {
      try {
        react {
          case Wakeup() => {
            doWithProb((0.05, { () =>
              prop.addImage(id, randomImage, randomImage, "...", (new Date()).getTime() / 1000)
            }), (0.01, { () =>
              val vid = randomVideo
              val thumb = "http://img.youtube.com/vi/" + vid + "/default.jpg";
              prop.addYoutube(id, vid, thumb, "...", (new Date()).getTime() / 1000)
            }), (0.01, { () =>
              val id = randomVideoInProp(prop).optString("id")
              prop.upvoteVideo(id)
            }), (0.01, { () =>
              val id = randomVideoInProp(prop).optString("id")
              prop.downvoteVideo(id)
            }), (0.01, { () =>
              val toId = randomUserInProp(prop).optString("id")
              prop.addRelationship(
                relations.toArray,
                reverseRelations.toArray,
                id, toId, randomRelation)
            }))
          }
          case 'kill => {
	    jxActor.leave()
	    exit
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
