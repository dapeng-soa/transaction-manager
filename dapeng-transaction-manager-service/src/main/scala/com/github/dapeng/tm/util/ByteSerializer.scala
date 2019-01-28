package com.github.dapeng.tm.util

import com.github.dapeng.core.BeanSerializer
import com.github.dapeng.org.apache.thrift.protocol.TProtocol

class ByteSerializer extends BeanSerializer[Array[Byte]] {
  override def read(iproto: TProtocol): Array[Byte] = {
    Array.emptyByteArray
  }

  override def write(bean: Array[Byte], oproto: TProtocol): Unit = {
    oproto.getTransport.write(bean)
  }

  override def validate(bean: Array[Byte]): Unit = {}

  override def toString(bean: Array[Byte]): String = {
    ""
  }
}
