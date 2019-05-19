
/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the LGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at  http://www.gnu.org/licenses/lgpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.fangjinuo.sqlhelper.util;

public class StringMaker {
    private String value;
    private String indexing;
    private int begin;
    private int end;

    public StringMaker(final String value) {
        this.value = value;
        this.indexing = value;
        this.end = value.length();
    }

    private StringMaker(final String value, final int begin, final int end) {
        this.value = value;
        this.indexing = value;
        this.begin = begin;
        this.end = end;
    }

    public StringMaker lower() {
        this.indexing = this.indexing.toLowerCase();
        return this;
    }

    public StringMaker upper() {
        this.indexing = this.indexing.toUpperCase();
        return this;
    }

    public StringMaker reset() {
        this.indexing = this.value;
        return this;
    }

    public StringMaker after(final char ch) {
        final int index = this.indexing.indexOf(ch, this.begin);
        if (index < 0 || index > this.end) {
            return this;
        }
        this.begin = ((index + 1 > this.end) ? this.end : (index + 1));
        return this;
    }

    public StringMaker after(final String ch) {
        final int index = this.indexing.indexOf(ch, this.begin);
        if (index < 0 || index > this.end) {
            return this;
        }
        this.begin = ((index + ch.length() > this.end) ? this.end : (index + ch.length()));
        return this;
    }

    public StringMaker before(final char ch) {
        final int index = this.indexing.indexOf(ch, this.begin);
        if (index < 0 || index > this.end) {
            return this;
        }
        this.end = ((index < this.begin) ? this.begin : index);
        return this;
    }

    public StringMaker before(final char ch1, final char ch2) {
        final int index = this.indexOf(ch1, ch2);
        if (index < 0 || index > this.end) {
            return this;
        }
        this.end = ((index < this.begin) ? this.begin : index);
        return this;
    }

    private int indexOf(final char ch1, final char ch2) {
        for (int i = this.begin; i < this.indexing.length(); ++i) {
            final char c = this.indexing.charAt(i);
            if (c == ch1 || c == ch2) {
                return i;
            }
        }
        return -1;
    }

    public StringMaker before(final String ch) {
        final int index = this.indexing.indexOf(ch, this.begin);
        if (index < 0 || index > this.end) {
            return this;
        }
        this.end = ((index < this.begin) ? this.begin : index);
        return this;
    }

    public StringMaker afterLast(final char ch) {
        final int index = this.indexing.lastIndexOf(ch, this.end);
        if (index < this.begin) {
            return this;
        }
        this.begin = ((index + 1 > this.end) ? this.end : (index + 1));
        return this;
    }

    public int getBeginIndex() {
        return this.begin;
    }

    public int getEndIndex() {
        return this.end;
    }

    public StringMaker afterLast(final char ch1, final char ch2) {
        final int index = this.lastIndexOf(this.indexing, this.end, ch1, ch2);
        if (index < this.begin) {
            return this;
        }
        this.begin = ((index + 1 > this.end) ? this.end : (index + 1));
        return this;
    }

    int lastIndexOf(final String string, final int end, final char ch1, final char ch2) {
        for (int i = end; i >= this.begin; --i) {
            final char c = string.charAt(i - 1);
            if (ch1 == c || ch2 == c) {
                return i - 1;
            }
        }
        return -1;
    }

    public StringMaker afterLast(final String ch) {
        final int index = this.indexing.lastIndexOf(ch, this.end);
        if (index < this.begin) {
            return this;
        }
        this.begin = ((index + ch.length() > this.end) ? this.end : (index + ch.length()));
        return this;
    }

    public StringMaker beforeLast(final char ch) {
        final int index = this.indexing.lastIndexOf(ch, this.end);
        if (index < this.begin) {
            return this;
        }
        this.end = index;
        return this;
    }

    public StringMaker beforeLast(final char ch1, final char ch2) {
        final int index = this.lastIndexOf(this.indexing, this.end, ch1, ch2);
        if (index < this.begin) {
            return this;
        }
        this.end = index;
        return this;
    }

    public StringMaker beforeLast(final String ch) {
        final int index = this.indexing.lastIndexOf(ch, this.end);
        if (index < this.begin) {
            return this;
        }
        this.end = index;
        return this;
    }

    public StringMaker prev() {
        this.end = this.begin;
        this.begin = 0;
        return this;
    }

    public StringMaker next() {
        this.begin = this.end;
        this.end = this.indexing.length();
        return this;
    }

    public StringMaker clear() {
        this.begin = 0;
        this.end = this.indexing.length();
        return this;
    }

    public boolean isEmpty() {
        return this.begin == this.end;
    }

    public String value() {
        return this.value.substring(this.begin, this.end);
    }

    public StringMaker duplicate() {
        return new StringMaker(this.value, this.begin, this.end);
    }

    @Override
    public String toString() {
        return this.value();
    }
}