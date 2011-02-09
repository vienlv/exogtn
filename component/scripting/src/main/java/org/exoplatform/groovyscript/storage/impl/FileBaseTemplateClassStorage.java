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
import org.exoplatform.groovyscript.storage.TemplateIOException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 2/7/11
 */
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
      }
      finally
      {
         Safe.close(out);
      }
   }
}
