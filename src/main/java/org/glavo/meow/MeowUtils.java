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

public final class MeowUtils {
    public static boolean isBlank(XWPFRun term) {
        return term.getEmbeddedPictures().isEmpty() && term.text().isBlank();
    }

    public static int findEnd(List<XWPFRun> expression, Meow startBracket, int startBracketIndex) {
        int end = startBracketIndex + 1;
        while (end < expression.size()) {
            if (startBracket.equals(Meow.of(expression.get(end)))) {
                return end;
            }
            end++;
        }
        return end;
    }

    private MeowUtils() {
    }
}
