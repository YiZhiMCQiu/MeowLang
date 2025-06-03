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

import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.util.List;

public sealed interface MeowExpression extends MeowValue {

    MeowValue eval(MeowContext context);

    @Override
    default MeowValue apply(MeowContext context, List<MeowExpression> args) {
        return eval(context).apply(context, args);
    }

    record Identifier(Meow meow) implements MeowExpression {

        @Override
        public MeowValue eval(MeowContext context) {
            return context.getValue(meow);
        }

    }

    record ExpressionList(List<MeowExpression> nodes) implements MeowExpression {
        @Override
        public MeowValue eval(MeowContext context) {
            if (nodes.isEmpty()) {
                return MeowUnit.INSTANCE;
            }

            return nodes.getFirst().apply(context, nodes.subList(1, nodes.size()));
        }

    }

    record RichText(List<XWPFRun> content) implements MeowExpression {
        @Override
        public MeowValue eval(MeowContext context) {
            return new MeowText(content);
        }
    }
}
