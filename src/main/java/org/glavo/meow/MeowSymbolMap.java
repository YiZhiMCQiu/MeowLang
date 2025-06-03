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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MeowSymbolMap {

    public static final MeowSymbolMap INSTANCE = new MeowSymbolMap();

    final Map<Meow, String> builtinSymbols = new LinkedHashMap<>();
    final Map<Meow, String> userSymbols = new LinkedHashMap<>();

    public Meow registerBuiltin(Meow meow, String name) {
        if (!builtinSymbols.containsKey(meow)) {
            builtinSymbols.put(meow, "@" + name);
        }
        return meow;
    }

    public String get(Meow meow) {
        String name = builtinSymbols.get(meow);
        if (name != null) {
            return name;
        }

        name = userSymbols.computeIfAbsent(meow, k -> "$" + userSymbols.size());
        return name;
    }

    public String toString(List<MeowExpression> nodes) {
        return nodes.stream().map(it -> it.toDebugString(this))
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
