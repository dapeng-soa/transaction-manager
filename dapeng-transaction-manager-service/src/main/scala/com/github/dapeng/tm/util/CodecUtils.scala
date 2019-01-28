package com.github.dapeng.tm.util

import java.nio.ByteBuffer

import com.github.dapeng.client.netty.TSoaTransport
import com.github.dapeng.core.helper.SoaHeaderHelper
import com.github.dapeng.core._
import com.github.dapeng.impl.plugins.netty.SoaMessageProcessor
import com.github.dapeng.org.apache.thrift.protocol.{TCompactProtocol, TProtocol}
import com.github.dapeng.util.{SoaMessageBuilder, TCommonTransport}
import com.today.soa.idgen.scala.IDServiceSuperCodec.{GenId_argsSerializer, genId_args}
import com.today.soa.idgen.scala.domain.GenIDRequest
import com.today.soa.idgen.scala.domain.serializer.GenIDRequestSerializer
import io.netty.buffer.{AbstractByteBufAllocator, PooledByteBufAllocator}

class RawBinarySerializer extends BeanSerializer[Array[Byte]] {
  override def read(iproto: TProtocol): Array[Byte] = ???

  override def write(bean: Array[Byte], oproto: TProtocol): Unit = {
    val byteBuf = ByteBuffer.allocateDirect(bean.length).put(bean, 0, bean.length)
    oproto.writeBinary(byteBuf)
  }

  override def validate(bean: Array[Byte]): Unit = {}

  override def toString(bean: Array[Byte]): String = {
    ""
  }
}

object CodecUtils {
  val allocator: AbstractByteBufAllocator = PooledByteBufAllocator.DEFAULT
  private val connectionPool = null

  def callMethod(serviceName: String, version: String, methodName: String, params: Array[Byte]): Unit = {

  }
}

object CodecTest extends App {
  val orderReq = new SoaMessageBuilder[GenId_argsSerializer]

  val reqBuf = PooledByteBufAllocator.DEFAULT.buffer(8192)

  val builder = new SoaMessageBuilder[genId_args]

  val header = SoaHeaderHelper.buildHeader("com.today.soa.idgen.service.IDService", "1.0.0", "genId")

  val req = genId_args(GenIDRequest("order_id", 10))

  val reqSerializer = new GenId_argsSerializer()

  val buf = builder.buffer(reqBuf).header(header)
    .body(req, reqSerializer)
    .seqid(10).build()


  val inputSoaTransport = new TSoaTransport(buf)
  val parser = new SoaMessageProcessor(inputSoaTransport)


  val context = TransactionContext.Factory.createNewInstance()
  // parser.service, version, method, header, bodyProtocol
  val soaHeader = parser.parseSoaMessage(context)


  val contentProtocol = parser.getContentProtocol
  buf.markReaderIndex
  val reqBytes = new Array[Byte](buf.readableBytes)
  buf.readBytes(reqBytes)
  buf.resetReaderIndex

  val reqArgs: genId_args = reqSerializer.read(contentProtocol)
  println(reqArgs)
  context.setAttribute("RAW_REQ", reqBytes)
  contentProtocol.readMessageEnd()

  val transport = new TCommonTransport(reqBytes, TCommonTransport.Type.Read)
  val protocol = new TCompactProtocol(transport)
  println(reqSerializer.read(protocol))

  //序列化
  val reqBytes2 = new Array[Byte](reqBytes.length)
  val transport2 = new TCommonTransport(reqBytes2, TCommonTransport.Type.Write)
  val protocol2 = new TCompactProtocol(transport2)
  reqSerializer.write(reqArgs, protocol2)

  //反序列化
  val transport3 = new TCommonTransport(reqBytes2, TCommonTransport.Type.Read)
  val protocol3 = new TCompactProtocol(transport3)
  println(reqSerializer.read(protocol3))




}
