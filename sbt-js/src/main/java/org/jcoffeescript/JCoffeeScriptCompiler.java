/*
 * Copyright 2010 David Yeung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jcoffeescript;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.NativeObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;

public class JCoffeeScriptCompiler {

    private final Scriptable globalScope;
    private final Options options;

    public JCoffeeScriptCompiler(String compilerUrl) {
        this(compilerUrl, Collections.<Option>emptyList());
    }

	public JCoffeeScriptCompiler(String compilerUrl, Collection<Option> options) {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(compilerUrl);
        try {
            try {
                Reader reader = new InputStreamReader(inputStream, "UTF-8");
                try {
                    Context context = Context.enter();
                    context.setOptimizationLevel(-1); // Without this, Rhino hits a 64K bytecode limit and fails
                    try {
                        globalScope = context.initStandardObjects();
                        context.evaluateReader(globalScope, reader, "coffee-script.js", 0, null);
                    } finally {
                        Context.exit();
                    }
                } finally {
                    reader.close();
                }
            } catch (UnsupportedEncodingException e) {
                throw new Error(e); // This should never happen
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new Error(e); // This should never happen
        }

        this.options = new Options(options);
    }

	public String compile (String coffeeScriptSource) throws JCoffeeScriptCompileException {
        Context context = Context.enter();
        try {
            Scriptable compileScope = context.newObject(globalScope);
            compileScope.setParentScope(globalScope);
            compileScope.put("coffeeScriptSource", compileScope, coffeeScriptSource);
            try {
                Object result = context.evaluateString(compileScope, String.format("CoffeeScript.compile(coffeeScriptSource, %s);", options.toJavaScript()),
                        "JCoffeeScriptCompiler", 0, null);

                System.out.println(((NativeObject)result).get("v3SourceMap"));

                String js = result instanceof String ? (String)result : ((NativeObject)result).get("js").toString();
                return js;
            } catch (JavaScriptException e) {
                throw new JCoffeeScriptCompileException(e);
            }
        } finally {
            Context.exit();
        }
    }


}
