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
package org.exoplatform.groovyscript;

import org.exoplatform.commons.utils.IOUtil;
import java.io.File;
import java.net.URL;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 2/10/11
 */
public class TemplateUtil
{
   public static GroovyScript generateScriptFromRawContent(String templateId, String templateName, String templateContent)
   {
      GroovyScriptBuilder builder = new GroovyScriptBuilder(templateId, templateName, templateContent);
      try{
         return builder.build();
      }
      catch(TemplateCompilationException ex)
      {
         ex.printStackTrace();
         return null;
      }
   }

   public static GroovyScript generateScriptFromFile(String relPath) throws Exception
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(relPath);

      File f = new File(url.toURI());
      String content = IOUtil.getFileContentAsString(f);

      return generateScriptFromRawContent(f.getName(), f.getName(), content);
   }
}
