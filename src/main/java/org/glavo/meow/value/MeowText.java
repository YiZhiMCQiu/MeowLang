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
package org.glavo.meow.value;

import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.glavo.meow.MeowContext;
import org.glavo.meow.MeowSymbolMap;
import org.glavo.meow.MeowUtils;
import org.glavo.meow.ast.MeowExpression;

import java.util.List;

public record MeowText(List<XWPFRun> content) implements MeowValue {
    @Override
    public MeowValue apply(MeowContext context, List<MeowExpression> args) {
        return this;
    }

    @Override
    public String toDebugString(MeowContext context, MeowSymbolMap symbolMap) {
        return MeowUtils.toDebugString(content);
    }

    @Override
    public String toDisplayString(MeowContext context) {
        StringBuilder builder = new StringBuilder();
        for (XWPFRun run : content) {
            builder.append(run.text());
        }
        return builder.toString();
    }
}
