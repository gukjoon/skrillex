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
    val _mongoConn = MongoConnection(new String(mongoUri.password),mongoUri.hosts(0).toInt)
    //val _mongoConn = MongoConnection("ds031087.mongolab.com",31087)
  
    val credsSplit = mongoUri.username.split(":")
    val username = credsSplit(0)
    val password = credsSplit(1)
    if (password != null)
      _mongoConn("heroku_app3056061").authenticate(username,password)
    val database = _mongoConn(mongoUri.database)("dubstep")
    
    def index = {
      var fb_user : String = null
      try {
	fb_user = FbGraph.getObject("me").get("id").getAsString
      }
      catch{
	case e : Exception  => fb_user = "null"
      }
      Text("Test")
      
      val count : Int = database.count.asInstanceOf[Int]
      val randGen = new scala.util.Random
      val rand = if (count > 0) randGen.nextInt(count); else 0;
      val msgs = database.find("msg" $exists true $ne "").limit(1).skip(rand)
      val msgStrings : Iterator[DubStepJoke] = msgs.map ( (obj : DBObject ) => DubStepJoke(obj.getOrElse("msg",""),obj.getOrElse("_id","none"),obj.getOrElse("votes","0")))
      
      html.index("What does dubstep sound like?",msgStrings.toSeq(0),fb_user)
    }
    
    def login = {
      Redirect("/")
    }
    
    def vote (direction : String, id : String) = {
      var fb_user : String = null
      try {
	fb_user = FbGraph.getObject("me").get("id").getAsString
      }
      catch{
	case e : Exception  => fb_user = null
      }
      
      val msg = database.findOne(MongoDBObject("_id" -> new ObjectId(id)))
      val query = MongoDBObject ("_id" -> new ObjectId(id),"voters" -> MongoDBObject("$ne" -> fb_user))
      val update = MongoDBObject("$inc" -> MongoDBObject("votes" -> 1), "$push" -> MongoDBObject("voters" -> fb_user))
      database.update(query,update)
      Redirect("/submission/" + id)
      //TODO: add response
      //TODO: no facebook user fail
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
