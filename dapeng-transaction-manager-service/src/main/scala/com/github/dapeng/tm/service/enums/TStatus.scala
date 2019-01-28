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
package com.github.dapeng.tm.service.enums

import wangzx.scala_commons.sql.DbEnum
 import wangzx.scala_commons.sql._ 
 class TStatus private(val id:Int, val name:String) extends DbEnum {
 	 override def toString(): String = "(" + id + "," + name + ")"  

 	 override def equals(obj: Any): Boolean = { 
 			 if (obj == null) false 
 			 else if (obj.isInstanceOf[TStatus]) obj.asInstanceOf[TStatus].id == this.id
 			 else false 
 	 } 

 	 override def hashCode(): Int = this.id 
 } 

 object TStatus {
	 val CREATED = new TStatus(1,"CREATED")
	 val SUCCEED = new TStatus(2,"SUCCEED")
	 val FAILED = new TStatus(3,"FAILED")
	 val DONE = new TStatus(4,"DONE")
	 def unknown(id: Int) = new TStatus(id, id+"")
	 def valueOf(id: Int): TStatus = id match {
 		 case 1 => CREATED 
 		 case 2 => SUCCEED 
 		 case 3 => FAILED 
 		 case 4 => DONE 
 		 case _ => unknown(id) 
 } 
 def apply(v: Int) = valueOf(v) 
 def unapply(v: TStatus): Option[Int] = Some(v.id)
 implicit object Accessor extends DbEnumJdbcValueAccessor[TStatus](valueOf)
}
