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

package com.github.dapeng.tm.service.exception

import com.github.dapeng.core.SoaBaseCodeInterface

/**
  * @author hui
  *         2019/1/17 0017 11:07 
  **/
class TmException(val errorCode: String, val message: String) extends SoaBaseCodeInterface {
  override def getMsg: String = message

  override def getCode: String = errorCode
}

object TmException {
  // Begin gtx error
  def beginGtxFailed(message: String) = new TmException("Err-Gtx-001", message)

  // Update gtx error
  def UpdateGtxFailed(message: String) = new TmException("Err-Gtx-002", message)

  // No such gtx
  def noGtx(message: String) = new TmException("Err-Gtx-003", message)

  // No such gtx step
  def noGtxStep(message: String) = new TmException("Err-Gtx-004", message)

  // Duplicated confirms
  def duplicationConfirm(message: String) = new TmException("Err-Gtx-005", message)

  // Try to confirm a failed gtx
  def confirmFailedGtx(message: String) = new TmException("Err-Gtx-006", message)

  // No gtx steps
  def noGtxSteps(message: String) = new TmException("Err-Gtx-007", message)

  //Gtx step confirm failed
  def getStepConfirmFailed(messages: String) = new TmException("Err-Gtx-008", messages)

  // Duplicated cancels
  def duplicationCancel(messages: String) = new TmException("Err-Gtx-009", messages)

  // Try to cancel a succeed gtx
  def cancelSucceedGtx(messages: String) = new TmException("Err-Gtx-010", messages)

  // Gtx step cancel failed
  def gtxStepCancelFailed(messages: String) = new TmException(" Err-Gtx-011", messages)

  // Update step error
  def UpdateStepFailed(message: String) = new TmException("Err-Gtx-012", message)

  // confirm Step Failed
  def confirmStepFailed(message: String) = new TmException("Err-Gtx-013", message)

  // cancel Step Failed
  def cancelStepFailed(message: String) = new TmException("Err-Gtx-014", message)
}
