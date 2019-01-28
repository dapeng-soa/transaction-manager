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

/**
* TM module of TCC for dapeng-soa
*
**/

/**
* 事务开启请求
**/
struct BeginGtxRequest {
/**
* 参与全局事务的服务名
**/
    1: string serviceName,
/**
* 参与全局事务的服务版本号
**/
    2: string version,
/**
* 参与全局事务的方法名
**/
    3: string method,
/**
* `try`阶段业务请求参数, 二进制方式
**/
    4: optional binary params,
/**
* `confirm`阶段的方法名, 默认为`s"${method}_confirm"`
**/
    5: optional string confirmMethod,
/**
* `cancel`阶段的方法名, 默认为`s"${method}_cancel"`
**/
    6: optional string cancelMethod,
/**
* 事务超时时间。过了超时时间后状态不是`完成`的话，会有定时器重试
**/
    7: optional i64 expiredAt
}

struct BeginGtxResponse {
/**
* 子事务id
**/
    1: i64 stepId,
/**
* 子事务序号，1为事务发起方序号，参与方依次递增
**/
    2: i16 stepSeq
}

/**
* 事务状态,1:新建(CREATED);2:成功(SUCCEED);3:失败(FAILED);4:完成(DONE)
**/
enum TxStatus {
/**
* 新建
**/
    CREATED = 1,
/**
* 成功
**/
    SUCCEED = 2,
/**
* 失败
**/
    FAILED = 3,
/**
* 完成
**/
    DONE = 4
}

/**
* confirm/cancel请求
**/
struct CcRequest {
/**
* 全局事务id
 **/
    1: i64 gtxId
}