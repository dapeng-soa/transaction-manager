package com.github.dapeng.tm.service.util

import com.today.soa.idgen.scala.IDServiceClient
import com.today.soa.idgen.scala.cache.IDCacheClient
import com.today.soa.idgen.scala.domain.GenIDRequest

import scala.annotation.tailrec

object GenIdUtil {

  private lazy val idServiceClient = new IDServiceClient // ID取号服务

  val GTX_ID = "gtx_id"
  val T_GTX_ID = "t_gtx_id"
  val T_GTX_STEP_ID= "t_gtx_step_id"

  /**
    * 获取主键id, 走缓存
    */
  def getId(bizTag: String): Long = {
    IDCacheClient.getId(bizTag)
  }

  /**
    * 批量取号
    *
    * @param bizTag 业务tag
    * @param step   取号步长，一般为1，
    *               如果要批量取号,比如step为10, 返回 100 - 109 这10个值
    * @return
    */
  def genIds(bizTag: String, step: Long): List[Long] = this.synchronized {

    @tailrec
    def gen(res: List[Long], step: Long): List[Long] = {
      step match {
        case a if a < 1 => res
        case a if a < 10 => res ::: (0 until step.toInt).map((_: Int) => getId(bizTag)).toList
        case a if a < 1024 => res ::: getIds(bizTag, step)
        case _ => gen(res ::: getIds(bizTag, 1023), step - 1023)
      }
    }
    gen(Nil, step)
  }

  private def getIds(bizTag: String, step: Long): List[Long] = {
    val start: Long = idServiceClient.genId(GenIDRequest(bizTag, step))
    start.until(start + step).toList
  }
}
