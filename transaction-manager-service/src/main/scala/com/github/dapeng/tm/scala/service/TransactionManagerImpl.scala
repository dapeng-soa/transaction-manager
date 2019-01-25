/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dapeng.tm.scala.service

import java.io.StringReader
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

import com.github.dapeng.client.netty.{JsonPost, TSoaTransport}
import com.github.dapeng.core.helper.SoaSystemEnvProperties
import com.github.dapeng.core.metadata.{Method, Service}
import com.github.dapeng.core.{SoaException, TransactionContext}
import com.github.dapeng.json.{JsonSerializer, OptimizedMetadata}
import com.github.dapeng.metadata.MetadataClient
import com.github.dapeng.org.apache.thrift.protocol.TBinaryProtocol
import com.github.dapeng.tm.scala.service.entity.{TGtx, TGtxStep}
import com.github.dapeng.tm.scala.service.exception.TmException
import com.github.dapeng.tm.scala.service.sql.TxQuery
/*import com.google.common.util.concurrent.ThreadFactoryBuilder*/
import com.today.service.commons.Assert
import io.netty.buffer.{AbstractByteBufAllocator, ByteBuf, PooledByteBufAllocator, UnpooledByteBufAllocator}
import javax.xml.bind.JAXB
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{Failure, Success}


@Transactional(rollbackFor = Array(classOf[Throwable]))
class TransactionManagerImpl extends TransactionManagerService {
  private val LOGGER = LoggerFactory.getLogger(getClass)
  private val serviceMetadata = new mutable.HashMap[String, OptimizedMetadata.OptimizedService]()
  /*  private val schedulerExecutorService: ScheduledExecutorService = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("dapeng-" + getClass.getSimpleName + "-scheduler-%d").build())*/

  /**
    *
    * *
    * # 开启全局事务
    * ## 业务描述
    *1. 通过gtxReq.gtxId，在t_gtx表中查询是否已存在该全局事务。如果没有，那么子事务序号为1，继续2；否则根据t_gtx_step的信息拿到子事务序号，跳去3，
    *2. 插入一条全局事务日志到t_gtx表中，状态为"新建", 备注为: 新建
    *3. 插入一条子事务日志到t_gtx_step表中， 状态为"新建", 备注为: 新建
    *4. 把子事务id以及序号返回
    * *
    * ## 接口依赖
    * 无
    * ## 输入
    * tm_vo.BeginGtxRequest
    * ## 输出
    * tm_vo.BeginGtxResponse
    * ## 异常码
    * Err-Gtx-001: Begin gtx error
    *
    **/
  override def beginGtx(gtxReq: BeginGtxRequest): BeginGtxResponse = {
    try {
      val isGtx: String = TransactionContext.Factory.currentInstance().getHeader.getCookies.get("gtxId")
      if (isGtx == null) {
        val gtxId = TxQuery.createGtx(gtxReq)
        TxQuery.createGtxStep(gtxReq, gtxId, true)
      } else {
        TxQuery.createGtxStep(gtxReq, isGtx.toLong, false)
      }
    } catch {
      case e: Throwable =>
        LOGGER.error(e.getMessage)
        throw new SoaException("Err-Gtx-001", "Begin gtx error")
    }
  }


  /**
    *
    * *
    * # 更新全局事务
    * ## 前置检查
    *1.通过gtxReq.gtxId，在t_gtx中查询是否已存在该全局事务。如果没有，那么抛异常"Err-Gtx-003"；
    * *
    * ## 业务逻辑
    *1. 更新全局事务状态, 备注为: 新建->成功/失败
    * *
    * ## 接口依赖
    * 无
    * ## 输入
    * tm_vo.UpdateGtxRequest
    * ## 输出
    * 无
    * ## 异常码
    * Err-Gtx-002:Update gtx error
    *
    * Err-Gtx-003:No such gtx
    *
    **/
  override def updateGtx(gtxReq: UpdateGtxRequest): Unit = {
    Assert.assert(!TxQuery.isGtx(gtxReq.gtxId), TmException.noGtx("No such gtx"))
    try {
      TxQuery.updateGtxStatus(gtxReq)
    } catch {
      case e: Throwable =>
        LOGGER.error(e.getMessage)
        throw new SoaException("Err-Gtx-002", "Update gtx error")
    }
  }

  /**
    *
    * *
    * # 更新zi事务
    * ## 前置检查
    *1.通过gtxReq.stepId, 在t_gtx_step中查询是否已存在该子事务。如果没有，那么抛异常"Err-Gtx-004"；
    * *
    * ## 业务逻辑
    *1. 更新子事务状态, 备注为: 新建->成功/失败
    * *
    * ## 接口依赖
    * 无
    * ## 输入
    * tm_vo.UpdateGtxRequest
    * ## 输出
    * 无
    * ## 异常码
    * Err-Gtx-012:Update gtx error
    *
    * Err-Gtx-004:No such gtx step
    *
    **/
  override def updateStep(stepReq: UpdateStepRequest): Unit = {
    val gtxStepId = TxQuery.isGtxStep(stepReq.stepId)
    Assert.assert(!gtxStepId.equals(0), TmException.noGtxStep("No such gtx step"))
    try {
      TxQuery.updateStepStatus(stepReq)
    } catch {
      case e: Throwable =>
        LOGGER.error(e.getMessage)
        throw new SoaException("Err-Gtx-012", "Update gtx error")
    }
  }

  /**
    *
    * *
    * # 确认全局事务
    * ## 前置检查
    *1. 通过gtxReq.gtxId，在t_gtx表中查询是否已存在该全局事务。如果没有，那么抛异常"Err-Gtx-003".
    *2. 判断全局事务日志状态，如果为"已完成"，那么抛异常"Err-Gtx-005";如果为"失败",那么抛异常"Err-Gtx-006"
    *2. 通过gtxReq.gtxId, 在t_gtx_step中查询该全局事务的所有子事务。 如果找不到，那么抛异常"Err-Gtx-007"
    * *
    * ## 业务逻辑
    *1. 从所有的子事务中找出状态不是"已完成"的记录，并根据同步异步标志，逐一调用各个子事务的confirm方法。
    *2. 对于confirm成功的子事务，更改状态为"已完成", 备注为: 新建->成功->已完成
    *3. 如果某个子事务confirm失败， 那么抛异常"Err-Gtx-008"(对于异步而言，某个子事务confirm失败不影响其它子事务；
    * 对于同步而言，某个子事务confirm失败后，序号比该失败子事务大的其它子事务，本轮不再尝试confirm)
    *4. 如果所有子事务confirm成功，更新全局事务状态为"已完成"，备注为: 新建->成功->已完成
    *5. 子事务的重试次数+1
    * *
    * ## 接口依赖
    * 无
    * ## 输入
    * tm_vo.confirmRequest
    * ## 输出
    * 无
    * *
    * ## 异常码
    * Err-Gtx-003:No such gtx
    *
    * Err-Gtx-005:Duplicated confirms
    *
    * Err-Gtx-006:Try to confirm a failed gtx
    *
    * Err-Gtx-007:No gtx steps
    *
    * Err-Gtx-008:Gtx step confirm failed
    *
    **/
  override def confirm(gtxReq: confirmRequest): Unit = {
    Assert.assert(!TxQuery.isGtx(gtxReq.gtxId), TmException.noGtx("No such gtx"))
    val status = TxQuery.getGtxStatus(gtxReq.gtxId)
    if (status.equals(3)) {
      Assert.assert(false, TmException.duplicationConfirm("Duplicated confirms"))
    } else if (status.equals(4)) {
      Assert.assert(false, TmException.confirmFailedGtx("Try to confirm a failed gtx"))
    }
    Assert.assert(TxQuery.getGtxSteps(gtxReq.gtxId).nonEmpty, TmException.noGtxSteps("No gtx steps"))

    updateGtx(new UpdateGtxRequest(gtxReq.gtxId, TxStatus.SUCCEED))

    val gtxSteps: List[TGtxStep] = TxQuery.getGtxSteps(gtxReq.gtxId).filterNot(x => x.status.id.equals(4)).sortWith((left, right) => left.stepSeq < right.stepSeq)

    try {
      if (TxQuery.isAsync(gtxReq.gtxId)) {
        confirmAsync(gtxSteps)
      } else {
        confirmSync(gtxSteps)
      }
    } catch {
      case e: Throwable => LOGGER.error(e.getMessage)
        throw new SoaException("Err-Gtx-008", "tx step confirm failed")
    }
    updateGtx(new UpdateGtxRequest(gtxReq.gtxId, TxStatus.DONE))
  }

  /**
    *
    * *
    * # 回滚全局事务
    * ## 前置检查
    *1. 通过gtxReq.gtxId，在t_gtx表中查询是否已存在该全局事务。如果没有，那么抛异常"Err-Gtx-003".
    *2. 判断全局事务日志状态，如果为"已完成"，那么抛异常"Err-Gtx-009";如果为"成功",那么抛异常"Err-Gtx-010"
    *2. 通过gtxReq.gtxId, 在t_gtx_step中查询该全局事务的所有子事务。 如果找不到，那么抛异常"Err-Gtx-007"
    * *
    * ## 业务逻辑
    *1. 从所有的子事务中找出状态不是"已完成"的记录，并根据同步异步标志，逐一调用各个子事务的cancel方法。
    *2. 对于cancel成功的子事务，更改状态为"已完成", 备注为: 新建->失败->已完成
    *3. 如果某个子事务cancel失败， 那么抛异常"Err-Gtx-011"(对于异步而言，某个子事务cancel失败不影响其它子事务；
    * 对于同步而言，某个子事务cancel失败后，序号比该失败子事务大的其它子事务，本轮不再尝试cancel)
    *4. 如果所有子事务cancel成功，更新全局事务状态为"已完成", 备注为: 新建->失败->已完成
    *5. 子事务的重试次数+1
    * *
    * ## 接口依赖
    * 无
    * ## 输入
    * tm_vo.cancelRequest
    * ## 输出
    * 无
    * *
    * ## 异常码
    * Err-Gtx-003:No such gtx
    *
    * Err-Gtx-007:No gtx steps
    *
    * Err-Gtx-009:Duplicated cancels
    *
    * Err-Gtx-010:Try to cancel a succeed gtx
    *
    * Err-Gtx-011:Gtx step cancel failed
    *
    **/
  override def cancel(gtxReq: cancelRequest): Unit = {
    Assert.assert(TxQuery.isGtx(gtxReq.gtxId), TmException.noGtx("No such gtx"))
    val status = TxQuery.getGtxStatus(gtxReq.gtxId)
    status match {
      case 3 => Assert.assert(false, TmException.duplicationCancel("Duplicated cancels"))
      case 4 => Assert.assert(false, TmException.cancelSucceedGtx("Try to cancel a succeed gtx"))
    }

    Assert.assert(TxQuery.getGtxSteps(gtxReq.gtxId).nonEmpty, TmException.noGtxSteps("No gtx steps"))

    updateGtx(new UpdateGtxRequest(gtxReq.gtxId, TxStatus.FAILED))

    val gtxSteps: List[TGtxStep] = TxQuery.getGtxSteps(gtxReq.gtxId).filterNot(x => x.status.id.equals(4)).sortWith((left, right) => left.stepSeq > right.stepSeq)

    try {
      if (TxQuery.isAsync(gtxReq.gtxId)) {
        cancelAsync(gtxSteps)
      } else {
        cancelSync(gtxSteps)
      }
    } catch {
      case e: Throwable => LOGGER.error(e.getMessage)
        throw new SoaException("Err-Gtx-011", "Gtx step cancel failed")
    }
    updateGtx(new UpdateGtxRequest(gtxReq.gtxId, TxStatus.DONE))
  }

  /**
    * 异步confirm
    *
    * 当一个子事务confirm失败，剩余子事务的处理？
    */
  def confirmAsync(gtxSteps: List[TGtxStep]): Unit = {
    val result = gtxSteps map (gtxStep => {
      Future {
        val confirmMethod = gtxStep.confirmMethodName.getOrElse(gtxStep.methodName + "_confirm")
        val request = gtxStep.request.orNull
        callServiceMethod(gtxStep.serviceName, gtxStep.version, confirmMethod, request, true, gtxStep.id)
      }
    })
    result.foreach(x => {
      x.onComplete({
        case Success(id) => updateStep(new UpdateStepRequest(id.asInstanceOf[Int], TxStatus.DONE))
        case Failure(ex) => LOGGER.error(ex.getMessage)
      })
    })
  }

  /**
    * 同步confirm
    *
    * 当一个子事务confirm失败，剩余子事务也取消confirm
    */
  def confirmSync(gtxSteps: List[TGtxStep]): Unit = {
    gtxSteps.foreach(gtxStep => {
      try {
        val confirmMethod = gtxStep.confirmMethodName.getOrElse(gtxStep.methodName + "_confirm")
        val request = gtxStep.request.orNull
        callServiceMethod(gtxStep.serviceName, gtxStep.version, confirmMethod, request, false, gtxStep.id)
        updateStep(new UpdateStepRequest(gtxStep.id, TxStatus.DONE))
      } catch {
        case e: Throwable =>
          LOGGER.error(e.getMessage)
          throw new SoaException("Err-Gtx-013", "confirm step failed")
      }
    })
  }

  /**
    * 异步cancel
    *
    * 当一个子事务cancel失败，剩余子事务的处理？
    */
  def cancelAsync(gtxSteps: List[TGtxStep]): Unit = {
    val result = gtxSteps map (gtxStep => {
      Future {
        val cancelMethod = gtxStep.confirmMethodName.getOrElse(gtxStep.methodName + "_cancel")
        val request = gtxStep.request.orNull
        callServiceMethod(gtxStep.serviceName, gtxStep.version, cancelMethod, request, true, gtxStep.id)
      }
    })
    result.foreach(x => {
      x.onComplete({
        case Success(id) => updateStep(new UpdateStepRequest(id.asInstanceOf[Int], TxStatus.DONE))
        case Failure(ex) => LOGGER.error(ex.getMessage)
      })
    })
  }

  /**
    * 同步cancel
    *
    * 当一个子事务cancel失败，剩余子事务也取消cancel
    */
  def cancelSync(gtxSteps: List[TGtxStep]): Unit = {
    gtxSteps.foreach(gtxStep => {
      try {
        val cancelMethod = gtxStep.confirmMethodName.getOrElse(gtxStep.methodName + "_cancel")
        val request = gtxStep.request.orNull
        callServiceMethod(gtxStep.serviceName, gtxStep.version, cancelMethod, request, false, gtxStep.id)
        updateStep(new UpdateStepRequest(gtxStep.id, TxStatus.DONE))
      } catch {
        case e: Throwable =>
          LOGGER.error(e.getMessage)
          throw new SoaException("Err-Gtx-014", "cancel step failed")
      }
    })
  }

  /**
    * 调用业务方的confirm/cancel方法
    *
    * 首先获取元数据，然后反序列化，通过jsonPost调用？
    **/
  def callServiceMethod(service: String, version: String, tccMethod: String, request: Array[Byte], isAsync: Boolean, gtxStepId: Int): Int = {
    var optimizedService: OptimizedMetadata.OptimizedService = serviceMetadata.get(service).orNull
    if (optimizedService == null) {
      val metadata: String = new MetadataClient(service, version).getServiceMetadata
      if (metadata != null) {
        val reader = new StringReader(metadata)
        optimizedService = new OptimizedMetadata.OptimizedService(JAXB.unmarshal(reader, classOf[Service]))
        serviceMetadata.+=((service, optimizedService))
      }
    }
    val jsonPost: JsonPost = new JsonPost(service, version, tccMethod)
    val method = optimizedService.getMethodMap.get(tccMethod)
    val req: OptimizedMetadata.OptimizedStruct = optimizedService.getOptimizedStructs.get(method.request.namespace + "." + method.request.name)

    val allocator: AbstractByteBufAllocator = if (SoaSystemEnvProperties.SOA_POOLED_BYTEBUF) {
      PooledByteBufAllocator.DEFAULT
    } else {
      UnpooledByteBufAllocator.DEFAULT
    }
    val buffer: ByteBuf = allocator.buffer(8192)
    if (request != null) {
      buffer.setBytes(0, request)
    } else {
      buffer.setZero(0,0)
    }
    val transport = new TSoaTransport(buffer)
    val jsonParams: String = new JsonSerializer(optimizedService, method, version, req).read(new TBinaryProtocol(transport, buffer.readableBytes(), buffer.readableBytes(), false, true))
    try {
      jsonPost.callServiceMethod(jsonParams, optimizedService)
      gtxStepId
    } catch {
      case e: Throwable =>
        LOGGER.error(e.getMessage)
        throw new SoaException("Err-Gtx-014", "call service method failed")
    } finally {
      buffer.release()
    }
  }

  /**
    * 定时任务
    *
    * 定时扫描子事务表，获取状态为非完成的事务，进行confirm/cancel
    **/
  /*  schedulerExecutorService.scheduleAtFixedRate(() => {
      val gtxWithNoDone: List[TGtx] = TxQuery.getGtxWithNoDone()

      gtxWithNoDone.foreach(gtx => {
        gtx.status.id match {
          case 1 =>
          case 2 => confirm(new confirmRequest(gtx.gtxId))
          case 3 => cancel(new cancelRequest(gtx.gtxId))
        }
      })

    }, 60, 60, TimeUnit.SECONDS)*/
}
