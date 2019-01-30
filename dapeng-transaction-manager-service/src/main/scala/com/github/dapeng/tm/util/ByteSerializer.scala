package com.github.dapeng.tm.util

import com.github.dapeng.core.BeanSerializer
import com.github.dapeng.org.apache.thrift.protocol.TProtocol
import com.github.dapeng.tm.TransactionManagerServiceSuperCodec.Confirm_resultSerializer

class ByteSerializer extends BeanSerializer[Array[Byte]] {
   /**
    * 空返回值的序列化器
    */
  val voidResultSerializer = new Confirm_resultSerializer
  override def read(iproto: TProtocol): Array[Byte] = {
    //consume the void struct
   voidResultSerializer.read(iproto)

    new Array[Byte](0)
  }

  override def write(bean: Array[Byte], oproto: TProtocol): Unit = {
    oproto.getTransport.write(bean)
  }

  override def validate(bean: Array[Byte]): Unit = {}

  override def toString(bean: Array[Byte]): String = {
    ""
  }
}
