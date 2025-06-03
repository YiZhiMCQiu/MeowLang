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

import java.text.MessageFormat;

/**
 *
 */
public final class MeowDebug {
    private static final boolean DEBUG = "true".equals(System.getProperty("meow.debug"));

    public static void debugLog(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }

    public static void debugLog(String pattern, Object... args) {
        if (DEBUG) {
            System.out.println(MessageFormat.format(pattern, args));
        }
    }
}
