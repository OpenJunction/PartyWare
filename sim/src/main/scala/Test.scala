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


import Data._

object Test {

  def testGraph() {
    val prop = new PartyProp("test")
    for (i <- (1 until 10)) {
      prop.updateUser("id" + i, "name" + i, "", randomPortrait)
    }
    val me = "X"
    prop.updateUser(me, "aemon", "", randomPortrait)
    prop.addRelationship(relations.toArray,
      reverseRelations.toArray, me, "id1", randomRelation)
    prop.addRelationship(relations.toArray,
      reverseRelations.toArray, "id1", "id2", randomRelation)
    println(prop.getRelationship(me, "id1"))

    val paths = prop.computeShortestPaths(me)

    val pathTo1 = paths.toMap.get("id1")
    for (p <- pathTo1)
      println(prop.prettyPathString(me, p))

    val pathTo2 = paths.toMap.get("id2")
    for (p <- pathTo2)
      println(prop.prettyPathString(me, p))

  }


}
