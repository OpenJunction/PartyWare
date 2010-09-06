import scala.util.Random._
import org.json.JSONObject
import edu.stanford.junction.sample.partyware._
import scala.collection.JavaConversions._

object Data{

  def randomUserInProp(prop:PartyProp) = {
    val users: List[JSONObject] = prop.getUsers.toList
    shuffle(users).head
  }


  def randomVideoInProp(prop:PartyProp) = {
    val vids: List[JSONObject] = prop.getYoutubeVids.toList
    shuffle(vids).head
  }


  def randomPortrait: String = {
    val images: List[String] = List(
      "http://www.historyplace.com/specials/portraits/presidents/t-obama.jpg",
      "http://www.historyplace.com/specials/portraits/presidents/gw-bush.jpg",
      "http://www.historyplace.com/specials/calendar/docs-pix/clinton.jpg",
      "http://www.historyplace.com/specials/calendar/docs-pix/reagan.jpg",
      "http://www.historyplace.com/specials/portraits/presidents/carter2.jpg",
      "http://www.historyplace.com/specials/portraits/presidents/ford.jpg"
    )
    shuffle(images).head
  }

  def randomImage: String = {
    val images: List[String] = List(
      "http://www.omgcritters.com/posters/bright-blue-red-eye-frog-on-green-leaves-staring-animal-picture.jpg",
      "http://www.omgcritters.com/posters/mouse-riding-frog-funny-cute-animal-pictures.jpg",
      "http://www.omgcritters.com/posters/cute-white-baby-harp-seal-on-ice-looking-up.jpg",
      "http://www.omgcritters.com/posters/funny-face-smiling-giraffe-goofy-animal-closeup-picture.jpg"
    )
    shuffle(images).head
  }

  def randomVideo: String = {
    val images: List[String] = List(
      "cXgY7NoEzYo",
      "o3qCK88aRJk",
      "GKXIZKP_ub4",
      "nVWYMl2yg1E"
    )
    shuffle(images).head
  }

  def randomName: String = {
    val images: List[String] = List(
      "Aemon",
      "Ben",
      "Monica",
      "Alex",
      "Michael",
      "David",
      "Rebecca",
      "Jim",
      "Oscar"
    )
    shuffle(images).head
  }

  val relations = List(
    "none",
    "friend",
    "old friend",
    "coworker",
    "ex-coworker",
    "employer",
    "employee",
    "teacher",
    "student",
    "significant other",
    "fiancé",
    "spouse"
  )

  val reverseRelations = List(
    "none",
    "friend",
    "old friend",
    "coworker",
    "ex-coworker",
    "employee",
    "employer",
    "student",
    "teacher",
    "significant other",
    "fiancé",
    "spouse"
  )

  def randomRelation = shuffle(relations.filterNot(_ == "none")).head


}
