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
package com.github.dapeng.tm.scala.service.sql

import java.sql.Timestamp

import com.github.dapeng.tm.scala.service.entity.{TGtx, TGtxStep}
import com.github.dapeng.tm.scala.service.enums.TStatus
import com.github.dapeng.tm.scala.service.util.GenIdUtil
import com.github.dapeng.tm.scala.service._
import wangzx.scala_commons.sql._
import com.today.service.commons.`implicit`.Implicits._

/**
  * @author hui
  *         2019/1/16 0016 10:39 
  **/
object TxQuery {
  private val dataSource = TmDataSource.Data

  def isGtx(gtxId: Long): Boolean = {
    val result = dataSource.rows[TGtx](
      sql"""select * from t_gtx where gtx_id= ${gtxId}"""
    )
    result.isEmpty
  }

  def isGtxStep(gtxStepId: Long): Int = {
    val result = dataSource.row[TGtxStep](
      sql"""select * from t_gtx_step where id = ${gtxStepId}"""
    )
    if (result.nonEmpty) {
      result.get.id
    } else {
      result.size
    }
  }

  def createGtx(gtxReq: BeginGtxRequest): Long = {
    val id = GenIdUtil.getId(GenIdUtil.T_GTX_ID)
    val gtxId = GenIdUtil.getId(GenIdUtil.GTX_ID)
    val now = new Timestamp(System.currentTimeMillis())
    val expiredAt = long2Date(gtxReq.expiredAt.getOrElse(System.currentTimeMillis() + 60000))
    val isAsync: Int = if (gtxReq.isAsync.getOrElse(true)) {
      1
    } else {
      0
    }

    dataSource.executeUpdate(
      sql"""
           insert into t_gtx set
           `id` = ${id},
           `gtx_id` = ${gtxId},
           `expired_time` = ${expiredAt},
           `status` = ${TStatus.CREATED},
           `async` = ${isAsync},
           `created_time` = ${now},
           `remark` = '新建'
         """)
    gtxId
  }

  def createGtxStep(gtxReq: BeginGtxRequest, gtxId: Long, isGtx: Boolean): BeginGtxResponse = {
    val id = GenIdUtil.getId(GenIdUtil.T_GTX_STEP_ID)
    val now = new Timestamp(System.currentTimeMillis())
    val optionSql: SQLWithArgs = gtxReq.params.optional(key => s",request = ${key}")
    val step_seq: Short = if (isGtx) {
      1
    } else {
      val result = dataSource.row[TGtxStep](sql"""select max(step_seq) from t_gtx_step where gtx_id = ${gtxId}""")
      (result.get.stepSeq + 1).asInstanceOf[Short]
    }
    val confirmMethod = gtxReq.confirmMethod.getOrElse(gtxReq.method + "_confirm")
    val cancelMethod = gtxReq.confirmMethod.getOrElse(gtxReq.method + "_cancel")
    dataSource.executeUpdate(
      sql"""
           insert into t_gtx_step set
           `id` = ${id},
           `gtx_id` = ${gtxId},
           `step_seq` = ${step_seq},
           `status` = ${TStatus.CREATED},
           `service_name` = ${gtxReq.serviceName},
           `version` = ${gtxReq.version},
           `method_name` = ${gtxReq.method},
           `confirm_method_name` = ${confirmMethod},
           `cancel_method_name` = ${cancelMethod},
           `redo_times` = 0,
           `created_time` = ${now},
           `remark` = '新建'
         """ + optionSql)
    new BeginGtxResponse(gtxId, id, step_seq)
  }

  def updateGtxStatus(gtxReq: UpdateGtxRequest): Unit = {
    val remarkChanged: String = gtxReq.status.id match {
      case 2 => "-> 成功"
      case 3 => "-> 失败"
      case 4 => "-> 完成"
    }
    dataSource.executeUpdate(
      sql"""update t_gtx set `status` = ${gtxReq.status.id},`remark` = concat(`remark`, ${remarkChanged}) where `gtx_id` = ${gtxReq.gtxId}"""
    )
  }

  def updateStepStatus(gtxReq: UpdateStepRequest): Unit = {
    val remarkChanged: String = gtxReq.status.id match {
      case 2 => "-> 成功"
      case 3 => "-> 失败"
      case 4 => "-> 完成"
    }
    dataSource.executeUpdate(
      sql"""update t_gtx_step set `status` = ${gtxReq.status.id},`remark` = concat(`remark`, ${remarkChanged}) where `id` = ${gtxReq.stepId}"""
    )
  }

  def getGtxStatus(gtxId: Long): Int = {
    val result = dataSource.row[TGtx](
      sql"""select * from t_gtx where `gtx_id` = ${gtxId}"""
    )
    result.get.status.id
  }

  def getGtxSteps(gtxId: Long): List[TGtxStep] = {
    dataSource.rows[TGtxStep](
      sql"""select * from t_gtx_step where `gtx_id` = ${gtxId};"""
    )
  }

  def isAsync(gtxId: Long): Boolean = {
    val result = dataSource.row[TGtx](
      sql"""select * from t_gtx where `gtx_id` = ${gtxId}"""
    ).forall(x => x.async.equals(1))
    result
  }

  def getGtxWithNoDone(): List[TGtx] = {
    dataSource.rows[TGtx](
      sql"""select * from t_gtx where `status` <> 4;"""
    )
  }

}
