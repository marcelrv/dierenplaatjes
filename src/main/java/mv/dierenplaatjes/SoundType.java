/*
 * Copyright (c) 2024.  Marcel Verpaalen
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package mv.dierenplaatjes;

public enum SoundType {
    ANIMAL("_Z", ""),
    COMBINED("_Q", ""),
    QUESTION("_Q", "A"),
    ANSWER("_Q", "C");

    private final String prefix;
    private final String suffix;

    SoundType(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }
}
