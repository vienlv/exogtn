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
package org.exoplatform.groovyscript.storage.impl;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.commons.utils.Safe;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.groovyscript.GroovyScript;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.groovyscript.storage.TemplateClassStorage;
import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.picocontainer.Startable;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 2/7/11
 */
@Managed
@NameTemplate({@Property(key = "view", value = "rootContainer")})
@ManagedDescription("FileBaseTemplateClassStorage")
public class FileBaseTemplateClassStorage extends TemplateClassStorage
{

   private static final File rootFile;

   static
   {

      String dataDir = PropertyManager.getProperty("gatein.data.dir");
      File dataFile = new File(dataDir);
      File _rootFile = null;
      if (dataFile.exists() && dataFile.isDirectory())
      {
         _rootFile = new File(dataFile, "templates");
         if (!_rootFile.exists())
         {
            _rootFile.mkdir();
         }
      }

      if (_rootFile != null && _rootFile.exists() && _rootFile.isDirectory())
      {
         rootFile = _rootFile;
      }
      else
      {
         rootFile = null;
      }
   }


   public FileBaseTemplateClassStorage(InitParams params) throws Exception
   {
      super(params);
   }


   @Override
   public GroovyScript load(String hashCode) throws Exception
   {
      GroovyScript script = null;

      File f = new File(rootFile,"" + hashCode);
      if(f.exists())
      {
         FileInputStream in = new FileInputStream(f);
         try{
            script = new GroovyScript(in, Thread.currentThread().getContextClassLoader());
         }
         finally
         {
            Safe.close(in);
            increaseOneRead();
         }
      }

      return script;
   }

   @Override
   public void store(GroovyScript script, String hashCode) throws Exception
   {
      File f = new File(rootFile,"" + hashCode);

      OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
      try{
         script.writeTo(out);
         out.flush();
      }
      finally
      {
         Safe.close(out);
         increaseOneWrite();
      }
      increaseOneSuccessfulWrite();
   }

   @Managed
   @ManagedDescription("Get current number of read operations")
   public long getReadCount()
   {
      return getReadCountNumber();
   }

   @Managed
   @ManagedDescription("Get current number of write operations")
   public long getWriteCount()
   {
      return getWriteCountNumber();
   }

   @Managed
   @ManagedDescription("Get current number of successful write operations")
   public long getSavedCount()
   {
      return getSavedCountNumber();
   }
}
