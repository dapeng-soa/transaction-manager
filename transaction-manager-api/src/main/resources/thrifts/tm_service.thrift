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
2. 插入一条全局事务日志到t_gtx表中，状态为`新建`, 备注为: 新建
3. 插入一条子事务日志到t_gtx_step表中， 状态为`新建`, 备注为: 新建
4. 把子事务id以及序号返回

## 接口依赖
 无
## 输入
tm_vo.BeginGtxRequest
## 输出
tm_vo.BeginGtxResponse
## 异常码
`Err-Gtx-001`: Begin gtx error
**/
tm_vo.BeginGtxResponse beginGtx(1: tm_vo.BeginGtxRequest gtxReq)

/**
# 更新全局事务
## 业务描述
### preCheck
1.通过gtxReq.gtxId，在t_gtx中查询是否已存在该全局事务。如果没有，那么抛异常`Err-Gtx-003`；
1.通过gtxReq.stepId, 在t_gtx_step中查询是否已存在该子事务。如果没有，那么抛异常`Err-Gtx-004`；

### 业务
1. 如果子事务的序号为1，那么该子事务为事务发起方，全局事务的最终状态跟事务发起方的状态一致。
2. 如果该子事务为事务发起方，那么更新全局事务状态, 备注为: 新建->成功/失败
3. 更新子事务状态, 备注为: 新建->成功/失败

## 接口依赖
 无
## 输入
tm_vo.UpdateGtxRequest
## 输出
无
## 异常码
`Err-Gtx-002`:Update gtx error

`Err-Gtx-003`:No such gtx

`Err-Gtx-004`:No such gtx step
**/
void updateGtx(1: tm_vo.UpdateGtxRequest gtxReq)

/**
# 确认全局事务
## 业务描述
### 前置检查
1. 通过gtxReq.gtxId，在t_gtx表中查询是否已存在该全局事务。如果没有，那么抛异常`Err-Gtx-003`.
2. 判断全局事务日志状态，如果为`已完成`，那么抛异常`Err-Gtx-005`;如果为`失败`,那么抛异常`Err-Gtx-006`
2. 通过gtxReq.gtxId, 在t_gtx_step中查询该全局事务的所有子事务。 如果找不到，那么抛异常`Err-Gtx-007`

### 业务
1. 从所有的子事务中找出状态不是`已完成`的记录，并根据同步异步标志，逐一调用各个子事务的confirm方法。
2. 对于confirm成功的子事务，更改状态为`已完成`, 备注为: 新建->成功->已完成
3. 如果某个子事务confirm失败， 那么抛异常`Err-Gtx-008`(对于异步而言，某个子事务confirm失败不影响其它子事务；
   对于同步而言，某个子事务confirm失败后，序号比该失败子事务大的其它子事务，本轮不再尝试confirm)
4. 如果所有子事务confirm成功，更新全局事务状态为`已完成`，备注为: 新建->成功->已完成
5. 子事务的重试次数+1

## 接口依赖
 无
## 输入
tm_vo.confirmRequest
## 输出
无

## 异常码
`Err-Gtx-003`:No such gtx

`Err-Gtx-005`:Duplicated confirms

`Err-Gtx-006`:Try to confirm a failed gtx

`Err-Gtx-007`:No gtx steps

`Err-Gtx-008`:Gtx step confirm failed
**/
void confirm(1: tm_vo.confirmRequest gtxReq)

/**
# 回滚全局事务
## 业务描述
### 前置检查
1. 通过gtxReq.gtxId，在t_gtx表中查询是否已存在该全局事务。如果没有，那么抛异常`Err-Gtx-003`.
2. 判断全局事务日志状态，如果为`已完成`，那么抛异常`Err-Gtx-009`;如果为`成功`,那么抛异常`Err-Gtx-010`
2. 通过gtxReq.gtxId, 在t_gtx_step中查询该全局事务的所有子事务。 如果找不到，那么抛异常`Err-Gtx-007`

### 业务
1. 从所有的子事务中找出状态不是`已完成`的记录，并根据同步异步标志，逐一调用各个子事务的cancel方法。
2. 对于cancel成功的子事务，更改状态为`已完成`, 备注为: 新建->失败->已完成
3. 如果某个子事务cancel失败， 那么抛异常`Err-Gtx-011`(对于异步而言，某个子事务cancel失败不影响其它子事务；
   对于同步而言，某个子事务cancel失败后，序号比该失败子事务大的其它子事务，本轮不再尝试cancel)
4. 如果所有子事务cancel成功，更新全局事务状态为`已完成`, 备注为: 新建->失败->已完成
5. 子事务的重试次数+1

## 接口依赖
 无
## 输入
tm_vo.cancelRequest
## 输出
无

## 异常码
`Err-Gtx-003`:No such gtx

`Err-Gtx-007`:No gtx steps

`Err-Gtx-009`:Duplicated concels

`Err-Gtx-010`:Try to cancel a succeed gtx

`Err-Gtx-011`:Gtx step cancel failed
**/
void cancel(1: tm_vo.cancelRequest gtxReq)

}