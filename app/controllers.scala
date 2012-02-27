package controllers

import play._
import play.mvc._
import com.mongodb.casbah.Imports._
import util.Properties

object Application extends Controller {
    
    import views.Application._
    
    private val mongoUri = new MongoURI(new com.mongodb.MongoURI(Properties.envOrElse("MONGOLAB_URI", "mongodb://127.0.0.1:27017/test")))
    //val _mongoConn = MongoConnection(new String(mongoUri.password),mongoUri.hosts(0).toInt)
    val _mongoConn = MongoConnection("ds031087.mongolab.com",31087)
  
    val credsSplit = mongoUri.username.split(":")
    val username = credsSplit(0)
    val password = credsSplit(1)
    if (password != null)
      _mongoConn("heroku_app3056061").authenticate(username,password)
    val database = _mongoConn(mongoUri.database)("dubstep")
    
    def index = {
//      Text(Properties.envOrElse("MONGOLAB_URI", "mongodb://127.0.0.1:27017/test") + "\n" + mongoUri.username + "\n" + mongoUri.hosts(0).toInt + "\n" + new String(mongoUri.database))

      val count : Int = database.count.asInstanceOf[Int]
      val randGen = new scala.util.Random
      val rand = if (count > 0) randGen.nextInt(count); else 0;
      val msgs = database.find("msg" $exists true $ne "").limit(1).skip(rand)
      val msgStrings = msgs.map ( (obj : DBObject ) => obj.getOrElse("msg","") )
      html.index("What does dubstep sound like?",msgStrings.toSeq)
    }
    
    def submitGet = {
	html.submit("")
    }
    
    def submitPost (msg : String) = {
	val doc = MongoDBObject("msg" -> msg, "random" -> Math.random)
	database.save( doc )
	Redirect("/")
    }
    
    def submitList (msg : String) = {
      val msgs = database.find("msg" $exists true $ne "")
      val msgStrings = msgs.map ( (obj : DBObject ) => obj.getOrElse("msg","") )
      html.submissions("List of submissions",msgStrings.toSeq)

    }
}
