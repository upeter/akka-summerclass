package controllers;

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.{ Iteratee, Enumerator }
import play.api.libs.concurrent.Promise
import akka.util.duration._
import akka.util._
import akka.actor.{ ActorSystem, Actor }
import play.api.libs.iteratee.Enumerator._
import akka.actor.Props
import akka.actor.Scheduler
import java.util.concurrent.atomic.AtomicInteger
import play.libs._
import com.xebia.actors.WebSocketPushActor
import com.xebia.actors._
/**
 * Controller for initiating websocket connection
 */
object WebSocketController extends Controller {

  val wsPushActor = Akka.system.actorOf(Props[WebSocketPushActor])
  val pusheeCounter = new AtomicInteger
  val counter = new AtomicInteger

  /**
   * Render initial page
   */
  def index() = Action {
    Ok(views.html.websockets.index())
  }

  /**
   * Establish websocket connection
   */
  def initWsConnection() = WebSocket.using[String] { request =>
    val in = Iteratee.foreach[String] { msg => wsPushActor ! PushMessage(msg) }    
    val count = pusheeCounter.incrementAndGet()
    val cpuInfoEnumerator = Enumerator.pushee[String](
      onStart = pushee => wsPushActor ! AddPushee(count, pushee),
      onComplete = wsPushActor ! RemovePushee(count),
      onError = (a, b) => println("error " + a + " " + b))
    (in, cpuInfoEnumerator)
  }

  /**
   * Push a Message to all Websockets
   */
  def sendPushMessage() = Action { request =>
    val msg = counter.incrementAndGet() + " by simple sender";
    wsPushActor ! PushMessage(msg)
    Ok("Sent message: " + msg)
  }

}


