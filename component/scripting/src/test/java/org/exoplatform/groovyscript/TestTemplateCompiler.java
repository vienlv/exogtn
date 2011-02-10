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

import org.apache.pdfbox.examples.pdmodel.HelloWorld;
import org.exoplatform.component.test.AbstractGateInTest;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestTemplateCompiler extends AbstractGateInTest
{

   /** . */
//   private final TemplateCompiler compiler = new TemplateCompiler();

   public void testFoo() throws IOException
   {
//      assertEquals("", compiler.compile(""));
//      assertEquals("out.print(\"a\");", compiler.compile("a"));
//      assertEquals("out.print(\"a\n\");\nout.print(\"b\");", compiler.compile("a\nb"));
//      assertEquals("", compiler.compile("<%%>"));
//      assertEquals("a", compiler.compile("<%a%>"));
//      assertEquals("a\nb", compiler.compile("<%a\nb%>"));
   }

   //TODO: Test template files containing closure with dynamic free variables

   public void testCompileHelloWorld() throws Exception
   {
       StringBuilder builder = new StringBuilder();
       builder.append("<% def message = \"Hello World!\" ;");
       builder.append("print message;");
       builder.append("%>");

       GroovyScript script = TemplateUtil.generateScriptFromRawContent("test", "test", builder.toString());

       CharArrayWriter arrayWriter = new CharArrayWriter(1024);

       script.render(new HashMap(), arrayWriter, Locale.US);

       assertEquals("Hello World!", arrayWriter.toString());
   }

}
