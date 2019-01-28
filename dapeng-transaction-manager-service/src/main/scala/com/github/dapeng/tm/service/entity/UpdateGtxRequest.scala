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

import com.github.dapeng.tm.service.TxStatus

/**
  * @author hui
  *         2019/1/28 0028 15:49 
  **/
case class UpdateGtxRequest (
  /**  全局事务id*/
  gtxId: Long,
  /** 全局事务状态, 1:新建(CREATED);2:成功(SUCCEED);3:失败(FAILED);4:完成(DONE) */
  status: TxStatus,
  )
