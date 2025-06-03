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

import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.glavo.meow.ast.MeowExpression;
import org.glavo.meow.ast.MeowIdentifier;
import org.glavo.meow.ast.MeowRichText;
import org.glavo.meow.ast.MeowExpressionList;

import java.util.ArrayList;
import java.util.List;

public final class MeowParser {
    public static MeowExpression parse(List<XWPFRun> expression, boolean top) {
        var meowExpressions = new ArrayList<MeowExpression>();

        for (int i = 0; i < expression.size(); i++) {
            XWPFRun term = expression.get(i);
            Meow meow = Meow.of(term);
            if (meow == null) {
                continue;
            }

            if (meow.isBracket() || meow.getUnderline() != UnderlinePatterns.NONE) {
                int end = MeowUtils.findEnd(expression, meow, i);

                List<XWPFRun> content = expression.subList(i + 1, end);

                if (meow.isBracket()) {
                    meowExpressions.add(parse(content, false));
                } else if (meow.getUnderline() == UnderlinePatterns.SINGLE) {
                    meowExpressions.add(new MeowRichText(content));
                } else {
                    // TODO: ???
                }

                i = end;
            } else {
                meowExpressions.add(new MeowIdentifier(meow));
            }
        }

        return top && meowExpressions.size() == 1
                ? meowExpressions.getFirst()
                : new MeowExpressionList(meowExpressions);
    }
}
