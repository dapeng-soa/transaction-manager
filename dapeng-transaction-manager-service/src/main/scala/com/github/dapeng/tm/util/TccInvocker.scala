package com.github.dapeng.tm.util

import java.util.ServiceLoader
import java.util.concurrent.{CompletableFuture, Future}

import com.github.dapeng.client.netty.JsonPost
import com.github.dapeng.core.helper.{DapengUtil, SoaSystemEnvProperties}
import com.github.dapeng.core.{InvocationContextImpl, SoaConnectionPoolFactory}
import com.github.dapeng.tm.util.TccInvocker.factory
import com.github.dapeng.util.InvocationContextUtils.capsuleContext
import org.slf4j.MDC

/**
  * Tcc invoker
  */
object TccInvocker {
  private val factory = ServiceLoader.load(classOf[SoaConnectionPoolFactory], classOf[JsonPost].getClassLoader).iterator.next
  private val byteSerializer = new ByteSerializer
}

class TccInvocker(val serviceName: String, val version: String, val method: String, val body: Array[Byte]) {
  private val pool = factory.getPool

  private val clientInfo = pool.registerClientInfo(serviceName, version)

  def invoke : CompletableFuture[Array[Byte]] = {
    val invocationContext = InvocationContextImpl.Factory.currentInstance
    capsuleContext(invocationContext.asInstanceOf[InvocationContextImpl], clientInfo.serviceName, clientInfo.version, method)

    val sessionTid = invocationContext.sessionTid.map(DapengUtil.longToHexStr(_)).orElse("0")

    val logLevel = invocationContext.cookie(SoaSystemEnvProperties.THREAD_LEVEL_KEY)

    if (logLevel != null) MDC.put(SoaSystemEnvProperties.THREAD_LEVEL_KEY, logLevel)

    MDC.put(SoaSystemEnvProperties.KEY_LOGGER_SESSION_TID, sessionTid)

    pool.sendAsync(serviceName, version, method, body, TccInvocker.byteSerializer, TccInvocker.byteSerializer).asInstanceOf[CompletableFuture[Array[Byte]]]
  }
}
