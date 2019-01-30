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
package com.github.dapeng.tm.service.entity

import java.sql.Timestamp
import com.github.dapeng.tm.service.enums.TStatus
import wangzx.scala_commons.sql.ResultSetMapper

  case class TGtxStep ( 
 /**  */ 
id: Int,
 /** 全局事务id，一般使用服务的会话id(sesstionTid) */ 
gtxId: Int,
 /** 子事务序号 */ 
stepSeq: Int,
 /** 子事务状态, 1:新建(CREATED);2:成功(SUCCEED);3:失败(FAILED);4:完成(DONE) */ 
status: TStatus,
 /** 服务名 */ 
serviceName: String,
 /** 服务版本号 */ 
version: String,
 /**  */ 
methodName: String,
 /**  */ 
request: Option[Array[Byte]],
 /**  */ 
confirmMethodName: Option[String],
 /**  */ 
cancelMethodName: Option[String],
 /**  */ 
retryTimes: Int,
 /** 创建时间 */ 
createdTime: Timestamp,
 /** 更新时间 */ 
updatedTime: Timestamp,
 /** 备注, 每次状态变更都需要追加到remark字段。 */ 
remark: String,
) 	
 	
 object TGtxStep { 
 	implicit val resultSetMapper: ResultSetMapper[TGtxStep] = ResultSetMapper.material[TGtxStep] 
 }
