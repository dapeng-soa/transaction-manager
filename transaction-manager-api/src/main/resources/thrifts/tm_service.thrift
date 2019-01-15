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

namespace java com.github.dapeng.tm.service

include "tm_vo.thrift"

/**
* TM module of TCC for dapeng-soa
*
**/
service TransactionManagerService {

/**
# 开启全局事务
## 业务描述
1. 通过gtxReq.gtxId，在t_gtx表中查询是否已存在该全局事务。如果没有，那么子事务序号为1，继续2；否则根据t_gtx_step的信息拿到子事务序号，跳去3，
2. 插入一条全局事务日志到t_gtx表中，状态为`新建`
3. 插入一条子事务日志到t_gtx_step表中， 状态为`新建`
4. 把子事务id以及序号返回

## 接口依赖
 无
## 输入
tm_vo.BeginGtxRequest
## 输出
tm_vo.BeginGtxResponse
**/
tm_vo.BeginGtxResponse beginGtx(1: tm_vo.BeginGtxRequest gtxReq)

}