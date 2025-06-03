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

import org.glavo.meow.ast.MeowExpression;
import org.glavo.meow.value.MeowBuiltinFunction;
import org.glavo.meow.value.MeowBuiltinMacro;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MeowSymbolMap {

    public static final MeowSymbolMap INSTANCE = new MeowSymbolMap();
    static {
        for (var macro : MeowBuiltinMacro.values()) {
            Meow meow = macro.getMeow();
            String name = macro.getName();
            if (!INSTANCE.builtinSymbols.containsKey(meow)) {
                INSTANCE.builtinSymbols.put(meow, "@" + name);
            }
        }

        for (var function : MeowBuiltinFunction.values()) {
            Meow meow = function.getMeow();
            String name = function.getName();
            if (!INSTANCE.builtinSymbols.containsKey(meow)) {
                INSTANCE.builtinSymbols.put(meow, "@" + name);
            }
        }
    }

    final Map<Meow, String> builtinSymbols = new LinkedHashMap<>();
    final Map<Meow, String> userSymbols = new LinkedHashMap<>();

    public String get(Meow meow) {
        String name = builtinSymbols.get(meow);
        if (name != null) {
            return name;
        }

        name = userSymbols.computeIfAbsent(meow, k -> "$" + userSymbols.size());
        return name;
    }

    public String toString(List<MeowExpression> nodes) {
        return nodes.stream().map(it -> it.toDebugString())
                .collect(Collectors.joining(", ", "[", "]"));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Symbols:\n");

        int maxLength = Math.max(
                builtinSymbols.values().stream().mapToInt(String::length).max().orElse(0),
                userSymbols.values().stream().mapToInt(String::length).max().orElse(0)
        ) + 1;

        Stream.concat(builtinSymbols.entrySet().stream(), userSymbols.entrySet().stream())
                .forEach(entry -> {
                    builder.append("  ").append(entry.getValue());
                    builder.repeat(' ', maxLength - entry.getValue().length());
                    builder.append(": ").append(entry.getKey()).append('\n');
                });

        if (builder.charAt(builder.length() - 1) == '\n') {
            builder.setLength(builder.length() - 1);
        }

        return builder.toString();
    }
}
