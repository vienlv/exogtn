/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.groovyscript.storage;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.groovyscript.GroovyScript;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 2/7/11
 */
public abstract class TemplateClassStorage
{
   private AtomicLong readCount;

   private AtomicLong writeCount;

   /** The number of successful write operation **/
   private AtomicLong savedCount;

   public TemplateClassStorage(InitParams params) throws Exception
   {
      this.readCount = new AtomicLong(0);
      this.writeCount = new AtomicLong(0);
      this.savedCount = new AtomicLong(0);
   }

   public abstract GroovyScript load(String hashCode) throws Exception ;

   public abstract void store(GroovyScript script, String hashCode) throws Exception ;

   protected long getReadCountNumber()
   {
      return readCount.get();
   }

   protected long getWriteCountNumber()
   {
      return writeCount.get();
   }

   protected long getSavedCountNumber()
   {
      return savedCount.get();
   }

   protected void increaseOneRead()
   {
      this.readCount.addAndGet(1);
   }

   protected void increaseOneWrite()
   {
      this.writeCount.addAndGet(1);
   }

   protected void increaseOneSuccessfulWrite()
   {
      this.savedCount.addAndGet(1);
   }

}
