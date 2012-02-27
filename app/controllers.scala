package controllers

import play._
import play.mvc._
import com.mongodb.casbah.Imports._

object Application extends Controller {
    
    import views.Application._
    
    val _mongoConn = MongoConnection()
    
    def index = {
	val msgs = _mongoConn("test")("test_data").find( "msg" $exists true $ne "" )
	val msgStrings = msgs.map( (obj: DBObject) => obj.getOrElse("msg","") )
	html.index("test",msgStrings.toSeq)
    }
    
    def submitGet = {
	html.submit(
    }
    
    def submitPost (msg : String) = {
	val doc = MongoDBObject("msg" -> msg)
	_mongoConn("test")("test_data").save( doc )
	Redirect("/")
    }
    
}
