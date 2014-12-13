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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;

import javax.script.*;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class JCoffeeScriptCompilerNashorn {

    private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
    static {
        ClassLoader classLoader = JCoffeeScriptCompilerNashorn.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("org/jcoffeescript/coffee-script-1.6.1.js");
        try {
            try {
                Reader reader = new InputStreamReader(inputStream, "UTF-8");
                try {
                    engine.eval(reader);
                } catch (ScriptException e) {
                    System.err.print("BLAH");
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
    }

    private final Options options;

    public JCoffeeScriptCompilerNashorn(String compilerUrl) {
        this(compilerUrl, Collections.<Option>emptyList());
    }

	public JCoffeeScriptCompilerNashorn(String compilerUrl, Collection<Option> options) {
        this.options = new Options(options);
    }

	public String compile (String coffeeScriptSource) throws JCoffeeScriptCompileException {
        Bindings b = new SimpleBindings();
        b.put("coffeeScriptSource", coffeeScriptSource);
        engine.setBindings(b, ScriptContext.GLOBAL_SCOPE);
        try {
            return (String)engine.eval(String.format("CoffeeScript.compile(coffeeScriptSource, %s);", options.toJavaScript()));
        } catch (Exception e) {
            throw new JCoffeeScriptCompileException(e);
        }
    }


}
