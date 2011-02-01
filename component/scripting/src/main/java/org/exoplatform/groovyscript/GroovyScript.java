/**
 * Copyright (C) 2009 eXo Platform SAS.
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

import groovy.lang.Binding;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.exoplatform.commons.utils.OutputStreamPrinter;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A wrapper for a Groovy script and its meta data.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class GroovyScript
{

   /** . */
   private final String templateId;

   /** . */
   private final String groovyText;

   /** . */
   private final Class<? extends BaseScript> scriptClass;

   /** . */
   private final Map<Integer, TextItem> lineTable;

   /** . */
   private final Map<String, byte[]> byteCodeMap;

   public GroovyScript(InputStream in, ClassLoader cl) throws IOException, ClassNotFoundException {

      ObjectInputStream data = new ObjectInputStream(in);

      //
      String templateId = data.readUTF();
      String groovyText = data.readUTF();
      String scriptClassName = data.readUTF();
      Map<Integer, TextItem> lineTable = (Map<Integer, TextItem>)data.readObject();
      final Map<String, byte[]> byteCodeMap = (Map<String, byte[]>)data.readObject();

      //
      ClassLoader cl2 = new ClassLoader(cl)
      {

         /** . */
         private Map<String, Class<?>> cache = new HashMap<String, Class<?>>();

         @Override
         protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
         {
            Class<?> clazz = cache.get(name);
            if (clazz == null)
            {
               byte[] byteCode = byteCodeMap.get(name);
               if (byteCode != null)
               {
                  clazz = defineClass(null, byteCode, 0, byteCode.length);
                  cache.put(name, clazz);
               }
               if (clazz == null)
               {
                  clazz = super.loadClass(name, resolve);
               }
            }
            return clazz;
         }
      };
      Class<?> scriptClass = cl2.loadClass(scriptClassName);

      //
      this.templateId = templateId;
      this.groovyText = groovyText;
      this.lineTable = lineTable;
      this.byteCodeMap = byteCodeMap;
      this.scriptClass = (Class<BaseScript>)scriptClass;
   }

   public GroovyScript(
      String templateId,
      String groovyText,
      Class<? extends BaseScript> scriptClass,
      Map<Integer, TextItem> lineTable,
      Map<String, byte[]> byteCodeMap)
   {
      this.templateId = templateId;
      this.groovyText = groovyText;
      this.scriptClass = scriptClass;
      this.lineTable = lineTable;
      this.byteCodeMap = byteCodeMap;
   }

   public void writeTo(OutputStream out) throws IOException {
      ObjectOutputStream data = new ObjectOutputStream(out);
      data.writeUTF(templateId);
      data.writeUTF(groovyText);
      data.writeUTF(scriptClass.getName());
      data.writeObject(lineTable);
      data.writeObject(byteCodeMap);
   }

   public String getTemplateId()
   {
      return templateId;
   }

   public String getGroovyText()
   {
      return groovyText;
   }

   public Class<? extends BaseScript> getScriptClass()
   {
      return scriptClass;
   }

   /**
    * Renders the script with the provided context and locale to the specified writer.
    *
    * @param context the context
    * @param writer the writer
    * @param locale the locale
    * @throws IOException
    * @throws TemplateRuntimeException
    */
   public void render(
      Map context,
      Writer writer,
      Locale locale) throws IOException, TemplateRuntimeException
   {
      Binding binding = context != null ? new Binding(context) : new Binding();

      //
      GroovyPrinter printer;
      if (writer instanceof OutputStreamPrinter)
      {
         printer = new OutputStreamWriterGroovyPrinter((OutputStreamPrinter)writer);
      }
      else
      {
         printer = new WriterGroovyPrinter(writer);
      }

      //
      printer.setLocale(locale);

      //
      BaseScript script = (BaseScript)InvokerHelper.createScript(scriptClass, binding);
      script.printer = printer;

      //
      try
      {
         script.run();
      }
      catch (Exception e)
      {
         if (e instanceof IOException)
         {
            throw (IOException)e;
         }
         else
         {
            throw buildRuntimeException(e);
         }
      }
      catch (Throwable e)
      {
         if (e instanceof Error)
         {
            throw ((Error)e);
         }
         throw buildRuntimeException(e);
      }

      //
      script.flush();
   }

   private TemplateRuntimeException buildRuntimeException(Throwable t)
   {
      StackTraceElement[] trace = t.getStackTrace();

      //
      TextItem firstItem = null;

      // Try to find the groovy script lines
      for (int i = 0;i < trace.length;i++)
      {
         StackTraceElement element = trace[i];
         if (element.getClassName().equals(scriptClass.getName()))
         {
            int lineNumber = element.getLineNumber();
            TextItem item = lineTable.get(lineNumber);
            int templateLineNumber;
            if (item != null)
            {
               templateLineNumber = item.getPosition().getLine();
               if (firstItem == null)
               {
                  firstItem = item;
               }
            }
            else
            {
               templateLineNumber = -1;
            }
            element = new StackTraceElement(
               element.getClassName(),
               element.getMethodName(),
               element.getFileName(),
               templateLineNumber);
            trace[i] = element;
         }
      }

      //
      t.setStackTrace(trace);

      //
      return new TemplateRuntimeException(templateId, firstItem, t);
   }
}
