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
import java.util.concurrent.CountDownLatch;

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

   private GroovyScript sharedTestedScript;

   public TestTemplateClassStorage() throws Exception
   {
      super();

      try{
         sharedTestedScript = TemplateUtil.generateScriptFromFile("UIPortalApplication.gtmpl");
      }
      catch(Exception ex)
      {
         sharedTestedScript = null;
      }
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
      assertNotNull(sharedTestedScript);
      assertEquals("UIPortalApplication.gtmpl", sharedTestedScript.getTemplateId());
   }

   public void testCompile()
   {
      try{
         GroovyScript script = TemplateUtil.generateScriptFromFile("UIPortalApplication.gtmpl");

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
      try{
         storageService.store(sharedTestedScript, "UIPortalApplication");
         GroovyScript loadScript = storageService.load("UIPortalApplication");
         assertNotNull(loadScript);

         assertEquals(loadScript.getGroovyText(), sharedTestedScript.getGroovyText());
      }
      catch(Exception ex)
      {
         fail();
      }
   }

   public void testSerializeRead ()
   {

   }

   public void testConcurrentWrite()
   {
      final CountDownLatch latch = new CountDownLatch(1);

      Thread[] threads = new Thread[20];

      for(int i = 0 ; i < 20; i++)
      {
         Runnable runnable = new Runnable()
         {
            public void run()
            {
               try{
                  latch.await();
                  storageService.store(sharedTestedScript, "UIPortalApplication");
               }
               catch(Exception ex)
               {
                  ex.printStackTrace();
               }
            }
         };

         threads[i] = new Thread(runnable);
         threads[i].start();
      }

      latch.countDown();

      try{
         for(int i =0; i< 20; i++)
         {
            threads[i].join();
         }
         }
      catch(InterruptedException ex)
      {
         ex.printStackTrace();
      }

      try{
         GroovyScript scriptReadFromMain = storageService.load("UIPortalApplication");
         assertEquals(sharedTestedScript.getScriptClass().getName(), scriptReadFromMain.getScriptClass().getName());
         assertEquals(sharedTestedScript.getGroovyText(), scriptReadFromMain.getGroovyText());
      }
      catch(Exception ex)
      {
          //ex.printStackTrace();
      }

   }

   public void testConcurrentWriteWithCrash()
   {

   }

   public void testConcurrentRead()
   {


   }

   /** Expect to get dirty read for the moment. Compare to testConcurrentWrite, there is no calls
    * to join()
    */
   public void testDirtyRead()
   {
      final CountDownLatch latch = new CountDownLatch(1);

            Thread[] threads = new Thread[20];

            for(int i = 0 ; i < 20; i++)
            {
               Runnable runnable = new Runnable()
               {
                  public void run()
                  {
                     try{
                        latch.await();
                        storageService.store(sharedTestedScript, "UIPortalApplication");
                     }
                     catch(Exception ex)
                     {
                        ex.printStackTrace();
                     }
                  }
               };

               threads[i] = new Thread(runnable);
               threads[i].start();
            }

            latch.countDown();

            //Read from main thread, there is no calls to join() on the above 20 threads, we expect failure for now
            try{
               GroovyScript scriptReadFromMain = storageService.load("UIPortalApplication");
               fail("EXPECT A FAILURE");
               assertEquals(sharedTestedScript.getScriptClass().getName(), scriptReadFromMain.getScriptClass().getName());
               assertEquals(sharedTestedScript.getGroovyText(), scriptReadFromMain.getGroovyText());
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }

   }

}
