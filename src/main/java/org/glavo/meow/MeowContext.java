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
import org.jline.terminal.Terminal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MeowContext {

    public static final MeowContext ROOT = new MeowContext(null, null);

    static {
        // Blue
        ROOT.setValue(Meow.builtin(0x00B0F0), (MeowMacro) MeowBuiltinFunctions::let);

        // Red
        ROOT.setValue(Meow.builtin(0xEE0000), (MeowFunction) MeowBuiltinFunctions::print);

        // Green
        ROOT.setValue(Meow.builtin(0x00B050), (MeowMacro) MeowBuiltinFunctions::lambda);
    }

    // ---------------

    private final MeowContext parent;
    private final Terminal terminal;
    private final Map<Meow, MeowValue> binding = new LinkedHashMap<>();

    public MeowContext(MeowContext parent, Terminal terminal) {
        this.parent = parent;
        this.terminal = terminal;
    }

    public Terminal getTerminal() {
        return terminal;
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
            debugLog("> evalExpr({0})", expr);
            expr.eval(this);
        }
    }

    // For debug...
    // Although it is very simple, it is enough for early development :)

    private static final boolean DEBUG = "true".equals(System.getProperty("meow.debug"));

    void debugLogBuiltinFunctionCall(String function, MeowContext context, List<?> args) {
        if (DEBUG) {
            terminal.writer().println(MessageFormat.format("  > {0}({1}, {2})", function, context, args));
        }
    }

    public void debugLog(String pattern, Object... args) {
        if (DEBUG) {
            terminal.writer().println(MessageFormat.format(pattern, args));
        }
    }
}
