/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.exoplatform.component.test.AbstractGateInTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestTemplateSerialization extends AbstractGateInTest
{

   public void testSerialization() throws Exception {

      GroovyScriptBuilder sb = new GroovyScriptBuilder("foo", "bar", "juu");
      GroovyScript script = sb.build();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      script.writeTo(baos);
      baos.close();
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      GroovyScript script2 = new GroovyScript(bais, Thread.currentThread().getContextClassLoader());
      assertNotNull(script2.getScriptClass());
      assertEquals(script.getGroovyText(), script2.getGroovyText());
      assertEquals(script.getTemplateId(), script2.getTemplateId());
      assertEquals(script.getScriptClass().getName(), script2.getScriptClass().getName());
      StringWriter sw = new StringWriter();
      script2.render(new HashMap(), sw, Locale.ENGLISH);
      assertEquals("juu", sw.toString());
   }
}
