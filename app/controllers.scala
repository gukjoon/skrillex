package controllers

import play._
import play.mvc._
import com.mongodb.casbah.Imports._
import util.Properties
import models._

import com.google.gson.JsonObject
import play.modules.facebook.FbGraph

object Application extends Controller {
    
    import views.Application._
    
    private val mongoUri = new MongoURI(new com.mongodb.MongoURI(Properties.envOrElse("MONGOLAB_URI", "mongodb://127.0.0.1:27017/test")))
    //MongoUri parsing is broken when passwords and usernames are in the mix ergo this nastiness
    val Array(host : String, portStr : String) = if (mongoUri.username == null) mongoUri.hosts(0).split(":") else Array(new String(mongoUri.password), mongoUri.hosts(0))
    val port : Int = portStr.toInt
    val _mongoConn = MongoConnection(host,port)
    val Array(username : Option[String], password : Option[String]) = if (mongoUri.username == null) Array(None,None) else mongoUri.username.split(":") map { value => Some(value) }

    if (username.isDefined && password.isDefined)
      _mongoConn(mongoUri.database).authenticate(username.get,password.get)
    
    val database = _mongoConn(mongoUri.database)("dubstep")
    
    def index = {
      var fb_user : String = null
      try {
	fb_user = FbGraph.getObject("me").get("id").getAsString
      }
      catch{
	case e : Exception  => fb_user = null
      }
      val count : Int = database.count.asInstanceOf[Int]
      if (count > 0) 
      {
	val randGen = new scala.util.Random
	val rand = randGen.nextInt(count)
	val msgs = database.find("msg" $exists true $ne "").limit(1).skip(rand)
	val msgStrings : Iterator[DubStepJoke] = msgs.map ( (obj : DBObject ) => DubStepJoke(obj.getOrElse("msg",""),obj.getOrElse("_id","none"),obj.getOrElse("votes","0")))
	html.index("What does dubstep sound like?",msgStrings.toSeq(0),fb_user)
      }
      else
      {
	html.empty("What does dubstep sound like?",fb_user)
      }
    }
    
    def login = {
      Redirect("/")
    }
    
    def vote (direction : String, id: String) = {
      //TODO: implement loan pattern for facebook user
      var fb_user : String = null
      try {
	fb_user = FbGraph.getObject("me").get("id").getAsString
      }
      catch{
	case e : Exception  => fb_user = null
      }
      
      if (fb_user != null) {
      
      val directionRev = if (direction == "up") "down" else "up"
      val dirAmount = if (direction == "up") 1 else -1
      val msg = database.findOne(MongoDBObject("_id" -> new ObjectId(id)))
      
      val query = MongoDBObject ("_id" -> new ObjectId(id),"voters." + fb_user -> direction)
      val queryRev = MongoDBObject ("_id" -> new ObjectId(id),"voters." + fb_user -> directionRev)
      val queryObj = MongoDBObject ("_id" -> new ObjectId(id))
      
      val count = database.count(query)
      val countRev = database.count(queryRev)


      var update : DBObject = null
      if (count == 0 && countRev == 0) //forward
	update = MongoDBObject("$inc" -> MongoDBObject("votes" -> dirAmount), "$push" -> MongoDBObject("voters" -> MongoDBObject(fb_user -> direction)))
      else if (count == 0 && countRev == 1) //zero out
	update = MongoDBObject("$inc" -> MongoDBObject("votes" -> dirAmount), "$pull" -> MongoDBObject("voters" -> MongoDBObject(fb_user -> directionRev)))
      else { //invalid
	Redirect("/submission/" + id + "?response=yes")
      }
      
      if (update != null)
      {
	database.update(queryObj,update)
	Text(database.find(queryObj).next.getOrElse("votes","Error"))
      }
      else	
      {
	Error("Duplicate vote")
      }
      }
      else {
	Error("No facebook user")
      }
    }
    
    def submitGet = {
	html.submit("")
    }
    
    def submitPost (msg : String) = {
	val doc = MongoDBObject("msg" -> msg, "votes" -> 0, "voters" -> List())
	database.save(doc)
	val newid = doc.getOrElse("_id","")
	Redirect("/submission/" + newid)
	//TODO: no facebook user fail
    }
    
    def submitList (msg : String) = {
      val msgs = database.find("msg" $exists true $ne "")
      val msgStrings = msgs.map ( (obj : DBObject ) => obj.getOrElse("msg","") )
      html.submissions("List of submissions",msgStrings.toSeq)
    }
    
    def getItem (item : String) = {
      //TODO: error if not exist
      var fb_user : String = null
      try {
	fb_user = FbGraph.getObject("me").get("id").getAsString
      }
      catch{
	case e : Exception  => fb_user = null
      }
      
      val msgs = database.find(MongoDBObject("_id" -> new ObjectId(item))).limit(1)
      val msgStrings : Iterator[DubStepJoke] = msgs.map ( (obj : DBObject ) => DubStepJoke(obj.getOrElse("msg",""),obj.getOrElse("_id","none"),obj.getOrElse("votes","0")))
      html.index("What does dubstep sound like?", msgStrings.toSeq(0),fb_user)
    }
}
