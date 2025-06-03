/*
 * Copyright (C) 2025 Glavo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.glavo.meow;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.glavo.meow.ast.MeowExpression;
import org.glavo.meow.value.MeowFunction;
import org.glavo.meow.value.MeowMacro;
import org.glavo.meow.value.MeowUnit;
import org.glavo.meow.value.MeowValue;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHighlightColor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MeowContext {

    public static final MeowContext ROOT = new MeowContext();

    static {
        // Light Blue
        registerBuiltinMacro(Meow.builtin(0x00B0F0, STHighlightColor.LIGHT_GRAY), "let", MeowBuiltinFunctions::let);

        // Green
        registerBuiltinMacro(Meow.builtin(0x00B050, STHighlightColor.LIGHT_GRAY), "lambda", MeowBuiltinFunctions::lambda);

        // Red
        registerBuiltinFunction(Meow.builtin(0xEE0000, STHighlightColor.LIGHT_GRAY), "print", MeowBuiltinFunctions::print);
    }

    private static void registerBuiltinMacro(Meow meow, String name, MeowMacro macro) {
        MeowSymbolMap.INSTANCE.registerBuiltin(meow, name);
        ROOT.setValue(meow, macro);
    }

    private static void registerBuiltinFunction(Meow meow, String name, MeowFunction function) {
        MeowSymbolMap.INSTANCE.registerBuiltin(meow, name);
        ROOT.setValue(meow, function);
    }

    // ---------------

    private final MeowContext parent;
    private final Terminal terminal;
    private final LineReader reader;
    private final Map<Meow, MeowValue> binding = new LinkedHashMap<>();


    // For ROOT
    private MeowContext() {
        this.parent = null;
        this.terminal = null;
        this.reader = null;
    }

    public MeowContext(MeowContext parent) {
        this.parent = parent;
        this.terminal = parent.terminal;
        this.reader = parent.reader;
    }

    public MeowContext(Terminal terminal) {
        this.parent = ROOT;
        this.terminal = terminal;
        this.reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public LineReader getReader() {
        return reader;
    }

    public MeowValue getValue(Meow key) {
        MeowContext context = this;
        while (context != null) {
            MeowValue value = context.binding.get(key);
            if (value != null) {
                return value;
            } else {
                context = context.parent;
            }
        }

        return MeowUnit.UNIT;
    }

    public void setValue(Meow key, MeowValue value) {
        binding.put(key, value);
    }

    public void evalFile(Path file) throws IOException {
        try (var xwpfDocument = new XWPFDocument(Files.newInputStream(file))) {
            evalDocument(xwpfDocument);
        }
    }

    public void evalDocument(XWPFDocument document) {
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            MeowExpression expr = MeowParser.parse(paragraph.getRuns(), true);
            log(">>> {0}", expr.toDebugString(MeowSymbolMap.INSTANCE));
            MeowValue result = expr.eval(this);
            log("|   >>> return({0}) in {1}", result.toDebugString(this, MeowSymbolMap.INSTANCE), this);
        }
    }

    // For debug...
    // Although it is very simple, it is enough for early development :)

    public void log(String pattern, Object... args) {
        if (Meow.DEBUG) {
            terminal.writer().println("[DEBUG] " + MessageFormat.format(pattern, args));
        }
    }

    void logFunctionCall(String function, List<?> args) {
        if (Meow.DEBUG) {
            log("|   >>> {0}({2}) in {1}",
                    function,
                    this,
                    args);
        }
    }

    void logMacroCall(String macro, List<MeowExpression> args) {
        if (Meow.DEBUG) {
            log("|   >>> {0}({2}) in {1}",
                    macro,
                    this,
                    MeowSymbolMap.INSTANCE.toString(args));
        }
    }

    @Override
    public String toString() {
        return "Context@" + Integer.toHexString(hashCode());
    }
}
