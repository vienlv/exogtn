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
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.RootContainer;
import org.exoplatform.groovyscript.storage.TemplateClassStorage;
import java.io.File;
import java.net.URL;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 2/9/11
 */
@ConfiguredBy({
   @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/templateClassStorage-rootConfiguration.xml")
})
public class TestTemplateClassStorage extends AbstractKernelTest
{

   /** Hack, as i don't know how to inject configuration.properties **/
   static{
      String tmpDir = System.getProperty("java.io.tmpdir");
      File templateFile = new File(tmpDir + System.getProperty("file.separator") + "templates");
      if(templateFile.exists() && templateFile.isDirectory())
      {
         File[] subFiles = templateFile.listFiles();

         for(File f : subFiles)
         {
            f.delete();
         }

         templateFile.delete();
      }

      PropertyManager.setProperty("gatein.data.dir", tmpDir);
   }

   private TemplateClassStorage storageService;

   public TestTemplateClassStorage() throws Exception
   {
      super();

   }

   public void setUp() throws Exception
   {
      super.setUp();
      RootContainer rootContainer = RootContainer.getInstance();
      storageService = (TemplateClassStorage)rootContainer.getComponentInstanceOfType(TemplateClassStorage.class);
   }

   public void tearDown() throws Exception
   {
      super.tearDown();
   }

   public void testInitService()
   {
      assertNotNull(storageService);
   }

   public void testCompile()
   {
      try{
         GroovyScript script = generateFromFile("UIPortalApplication.gtmpl");

         assertNotNull(script);
         assertEquals("UIPortalApplication.gtmpl", script.getTemplateId());

      }catch(Exception ex)
      {
         ex.printStackTrace();
         fail();
      }
   }

   public void testSerializeWrite()
   {

   }

   public void testSerializeRead ()
   {

   }

   public void testConcurrentWrite()
   {

   }

   public void testConcurrentWriteWithCrash()
   {

   }

   public void testConcurrentRead()
   {


   }

   public void testDirtyRead()
   {

   }


   private GroovyScript generateFromRawContent(String templateId, String templateName, String templateContent) throws Exception
   {
      GroovyScriptBuilder scriptBuilder = new GroovyScriptBuilder(templateId, templateName, templateContent);

      return scriptBuilder.build();
   }

   private GroovyScript generateFromFile(String relPath) throws Exception
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(relPath);
      File f = new File(url.toURI());
      String templateContent = IOUtil.getFileContentAsString(f);

      return generateFromRawContent(f.getName(), f.getName(), templateContent);
   }


}
