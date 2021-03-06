package org.hanrw.akka.seckill.actor.sku

import akka.actor.{Actor, ActorLogging, Props}
import org.hanrw.akka.seckill.actor.message.{SecKillFail, SecKillSuccess}
import org.hanrw.akka.seckill.actor.request.{SecKillCheckStockRequest, SecKillSkuNotAvailableRequest}

/**
 * 用于创建sku对应的actor
 * 每个sku存在多个商品实例
 *
 * @author hanrw
 * @date 2020/5/9 6:07 PM
 */
class SkuActorFactory(skuId: String, amountOfSku: Int) extends Actor with ActorLogging {
  override def preStart(): Unit = {
    initSkus(skuId, amountOfSku)
  }

  /**
   * @description 初始化商品sku
   *              每一个商品对应一个sku actor
   *              e.g. 一个sku有库存有5个
   *              那么每一个sku actor的path可以用以下方式表达
   *              sku:actor:01893:1
   *              sku:actor:01893:2
   *              sku:actor:01893:3
   *              sku:actor:01893:4
   *              sku:actor:01893:5
   * @date 9:42 PM 2020/5/9
   * @param skuId
   * @param amountOfSku
   * @return void
   **/
  private def initSkus(skuId: String, amountOfSku: Int): Unit = {
    log.info("init sku actors")
    0 until amountOfSku foreach {
      idx => context.actorOf(Props(new SkuActor(s"skuId:$idx")), s"sku:actor:$skuId:$idx")
    }
  }

  override def receive: Receive = updateAmountOfSku(amountOfSku)

  private def updateAmountOfSku(amountOfSku: Int): Receive = {
    case SecKillSuccess(userId, sku) if amountOfSku > 0 =>
      log.info(s"商品被成功抢购,库存还剩${amountOfSku - 1}")
      context.become(updateAmountOfSku(amountOfSku - 1))
      self ! SecKillCheckStockRequest(sku.skuId)
    case _: SecKillSkuNotAvailableRequest =>
      log.info(s"该商品已经被抢购完")
    case _: SecKillCheckStockRequest =>
      log.info(s"该商品库存还剩$amountOfSku,${System.currentTimeMillis()}")
    case SecKillFail =>
      log.info(s"该商品已经被抢购完")

  }
}

