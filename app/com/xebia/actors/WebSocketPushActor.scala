package com.xebia.actors
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
/**
 * Actor messages
 */
sealed trait WebSocketMessage
case class AddPushee(id: Int, pushee: Pushee[String]) extends WebSocketMessage
case class PushMessage(msg:String) extends WebSocketMessage
case class RemovePushee(id: Int) extends WebSocketMessage

/**
 * Actor that pushes messages to the websockets it controlls
 */
class WebSocketPushActor extends Actor {
  var pushees: Map[Int, Pushee[String]] = Map.empty

  def receive = {
    case AddPushee(id, pushee) => {
      println("Added pushee with id " + id)
      pushees = pushees + (id -> pushee)
    }
    case PushMessage(msg) => {
      if (!pushees.isEmpty) {
        println("Send to pushees: " + msg)
      }
      pushees.foreach {
        case (id, p) => {
          println("Send to pushee: " + id + ": " + msg)
          p.push(msg)
        }
      }
    }
    case RemovePushee(id) => {
      println("Remove pushee with id " + id)
      pushees = pushees - id

    }

  }
}