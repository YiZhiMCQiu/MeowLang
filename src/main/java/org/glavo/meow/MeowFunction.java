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

import java.util.List;

@FunctionalInterface
public non-sealed interface MeowFunction extends MeowValue {
    MeowValue applyValues(MeowContext context, List<MeowValue> args);

    @Override
    default MeowValue apply(MeowContext context, List<MeowExpression> args) {
        return applyValues(context, args.stream().map(arg -> arg.eval(context)).toList());
    }
}
