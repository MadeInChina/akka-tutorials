package org.hanrw.akka.seckill

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.hanrw.akka.seckill.actor.request.SecKillRequest
import org.hanrw.akka.seckill.actor.sku.SkuActorFactory

/**
 * seckill application
 */
object SecKillApplication {
  def main(args: Array[String]): Unit = {
    val amountOfSku = 10
    val users = 1000000
    val skuId = UUID.randomUUID().toString
    val system = ActorSystem("SecKill", ConfigFactory.load())
    system.actorOf(Props(new SkuActorFactory(skuId, amountOfSku)), "skus")
    Thread.sleep(1000)
    println(s"开始抢购 ${System.currentTimeMillis()}")
    0 until  users foreach { userId =>
      val idx = userId % amountOfSku
      val skuActor = system.actorSelection(s"/user/skus/sku:actor:$skuId:$idx")
      val secKillRequest = SecKillRequest(userId, skuId)
      skuActor ! secKillRequest
    }
    println("Done!")
  }
}