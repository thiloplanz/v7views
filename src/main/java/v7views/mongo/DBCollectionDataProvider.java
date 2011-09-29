/**
 * Copyright (c) 2011, Thilo Planz. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package v7views.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.click.control.Table;
import org.apache.click.dataprovider.PagingDataProvider;
import org.bson.BSONObject;

import v7views.v7db.BSONBackedObject;
import v7views.v7db.BSONBackedObjectLoader;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class DBCollectionDataProvider implements
		PagingDataProvider<BSONBackedObject> {

	private final V7Collection collection;

	private final Table table;

	private final DBObject orderBy;

	private final List<Object> ids;

	private final BSONBackedObject query;

	public DBCollectionDataProvider(V7Collection collection, Table table,
			BSONBackedObject orderBy, BSONBackedObject query,
			Collection<?> idFilter) {
		this.collection = collection;
		this.table = table;
		this.orderBy = orderBy == null ? new BasicDBObject("_id", 1) : orderBy
				.getDBObject();
		this.query = query;

		if (idFilter != null) {
			// first get all the ids, in the right order
			ids = new ArrayList<Object>(idFilter.size());

			for (DBObject o : collection.findIds(query).sort(this.orderBy)) {
				Object id = o.get("_id");
				if (idFilter.contains(id))
					ids.add(id);
			}
		} else {
			ids = null;
		}

	}

	public Iterable<BSONBackedObject> getData() {

		int start = table.getFirstRow();
		int pageSize = table.getPageSize();

		if (ids != null) {

			// skip and pageSize
			if (start >= ids.size())
				return Collections.emptyList();
			int toIndex = Integer.MAX_VALUE;
			if (pageSize > 0)
				toIndex = start + pageSize;
			if (toIndex > ids.size())
				toIndex = ids.size();
			final List<Object> page = ids.subList(start, toIndex);

			return new Iterable<BSONBackedObject>() {

				public Iterator<BSONBackedObject> iterator() {

					final Iterator<Object> p = page.iterator();

					Iterator<BSONObject> x = new Iterator<BSONObject>() {

						public boolean hasNext() {
							return p.hasNext();
						}

						public BSONObject next() {
							return collection.findOne(p.next());
						}

						public void remove() {
							throw new UnsupportedOperationException();
						}
					};

					return BSONBackedObjectLoader.wrapIterator(x, null);

				}

			};

		}

		DBCursor cursor = collection.find(query).sort(orderBy).skip(start);
		if (pageSize > 0)
			cursor = cursor.limit(pageSize);

		return BSONBackedObjectLoader.wrapIterable(cursor, null);
	}

	public int size() {
		if (ids != null)
			return ids.size();

		return (int) collection.count(query);
	}

}
