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
import org.glavo.meow.value.MeowBuiltinFunction;
import org.glavo.meow.value.MeowBuiltinMacro;
import org.glavo.meow.value.MeowUnit;
import org.glavo.meow.value.MeowValue;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MeowContext {

    public static final MeowContext ROOT = new MeowContext();

    static {
        for (var macro : MeowBuiltinMacro.values()) {
            ROOT.setValue(macro.getMeow(), macro);
        }
        for (var function : MeowBuiltinFunction.values()) {
            ROOT.setValue(function.getMeow(), function);
        }
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
            log(">>> [{0}]", expr.toDebugString());
            MeowValue result = expr.eval(this);
            log("|   >>> return {0} in {1}", result.toDebugString(this), this);
        }
    }

    // For debug...
    // Although it is very simple, it is enough for early development :)

    public void log(String pattern, Object... args) {
        if (Meow.DEBUG) {
            terminal.writer().println("[DEBUG] " + MessageFormat.format(pattern, args));
        }
    }

    @Override
    public String toString() {
        return "Context@" + Integer.toHexString(hashCode());
    }
}
