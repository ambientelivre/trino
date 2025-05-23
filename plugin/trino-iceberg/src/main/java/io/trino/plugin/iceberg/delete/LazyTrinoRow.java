/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.iceberg.delete;

import io.trino.spi.connector.SourcePage;
import io.trino.spi.type.Type;
import org.apache.iceberg.StructLike;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static io.trino.plugin.iceberg.IcebergPageSink.getIcebergValue;
import static java.util.Objects.requireNonNull;

/**
 * Lazy version of {@link TrinoRow}.
 */
final class LazyTrinoRow
        implements StructLike
{
    private final Type[] types;
    private final SourcePage page;
    private final int position;
    private final Object[] values;

    public LazyTrinoRow(Type[] types, SourcePage page, int position)
    {
        checkArgument(types.length == page.getChannelCount(), "mismatched types for page");
        this.types = requireNonNull(types, "types is null");
        this.page = requireNonNull(page, "page is null");
        checkElementIndex(position, page.getPositionCount(), "page position");
        this.position = position;
        this.values = new Object[types.length];
    }

    @Override
    public int size()
    {
        return page.getChannelCount();
    }

    @Override
    public <T> T get(int i, Class<T> clazz)
    {
        return clazz.cast(get(i));
    }

    @Override
    public <T> void set(int i, T t)
    {
        throw new UnsupportedOperationException();
    }

    private Object get(int i)
    {
        Object value = values[i];
        if (value != null) {
            return value;
        }

        value = getIcebergValue(page.getBlock(i), position, types[i]);
        values[i] = value;
        return value;
    }
}
