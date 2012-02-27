package controllers

import play._
import play.mvc._
import com.mongodb.casbah.Imports._

object Application extends Controller {
    
    import views.Application._
    
    val _mongoConn = MongoConnection("ds031087.mongolab.com",31087)
    
    def index = {
	val database = _mongoConn("test")("dubstep")
	val count : Int = database.count.asInstanceOf[Int]
	val randGen = new scala.util.Random
	val rand = randGen.nextInt(count)
	val msgs = database.find("msg" $exists true $ne "").limit(1).skip(rand)
	val msgStrings = msgs.map ( (obj : DBObject ) => obj.getOrElse("msg","") )
	html.index("What does dubstep sound like?",msgStrings.toSeq)
    }
    
    def submitGet = {
	html.submit("")
    }
    
    def submitPost (msg : String) = {
	val doc = MongoDBObject("msg" -> msg, "random" -> Math.random)
	_mongoConn("test")("dubstep").save( doc )
	Redirect("/")
    }
    
}
