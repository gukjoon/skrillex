package controllers

import play._
import play.mvc._
import com.mongodb.casbah.Imports._
import util.Properties

object Application extends Controller {
    
    import views.Application._
    
    private val mongoUri = new MongoURI(new com.mongodb.MongoURI(Properties.envOrElse("MONGOLAB_URI", "mongodb://127.0.0.1:27017/test")))
    val _mongoConn = MongoConnection(mongoUri)
    
    def index = {
	Text(mongoUri)
	/*
	if (mongoUri.password == null || _mongoConn("test").authenticate(mongoUri.username,mongoUri.password.asInstanceOf[String])){
	  val database = _mongoConn("test")("dubstep")
	  val count : Int = database.count.asInstanceOf[Int]
	  val randGen = new scala.util.Random
	  val rand = randGen.nextInt(count)
	  val msgs = database.find("msg" $exists true $ne "").limit(1).skip(rand)
	  val msgStrings = msgs.map ( (obj : DBObject ) => obj.getOrElse("msg","") )
	  html.index("What does dubstep sound like?",msgStrings.toSeq)	  
	}
	else {
	  html.index("What does dubstep sound like?",List("404 errors"))
	}*/
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
