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

import java.util.ArrayList;
import java.util.List;

public final class MeowParser {
    public static MeowExpression parse(List<XWPFRun> expression, boolean top) {
        var meowExpressions = new ArrayList<MeowExpression>();

        for (int i = 0; i < expression.size(); i++) {
            XWPFRun term = expression.get(i);
            if (MeowUtils.isBlank(term)) {
                continue;
            }

            Meow meow = Meow.of(term);
            if (meow != null) {
                if (meow.isBracket() || meow.getUnderline() != UnderlinePatterns.NONE) {
                    int end = MeowUtils.findEnd(expression, meow, i);

                    List<XWPFRun> content = expression.subList(i + 1, end);

                    if (meow.isBracket()) {
                        meowExpressions.add(parse(content, false));
                    } else if (meow.getUnderline() == UnderlinePatterns.SINGLE) {
                        meowExpressions.add(new MeowExpression.RichText(content));
                    } else {
                        // TODO: ???
                    }

                    i = end - 1;
                } else {
                    meowExpressions.add(new MeowExpression.Identifier(meow));
                }
            } else {
                ArrayList<XWPFRun> runs = new ArrayList<>();
                runs.add(term);

                int j = i + 1;
                while (j < expression.size()) {
                    XWPFRun run = expression.get(i);
                    if (Meow.of(run) == null) {
                        runs.add(run);
                        j++;
                    } else {
                        break;
                    }
                }

                i = j - 1;

                while (!runs.isEmpty()) {
                    if (MeowUtils.isBlank(runs.getLast())) {
                        runs.removeLast();
                    } else {
                        break;
                    }
                }

                if (!runs.isEmpty()) {
                    meowExpressions.add(new MeowExpression.RichText(runs));
                }
            }
        }

        return top && meowExpressions.size() == 1
                ? meowExpressions.getFirst()
                : new MeowExpression.ExpressionList(meowExpressions);
    }
}
