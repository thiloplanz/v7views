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

import v7views.v7db.BSONBackedObject;
import v7views.v7db.BSONBackedObjectLoader;
import v7views.v7db.SchemaDefinition;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class V7Collection {

	private final DBCollection collection;

	private final DBObject filter;

	private final static BasicDBObject nothing = new BasicDBObject();

	V7Collection(DBCollection collection) {
		this.collection = collection;
		this.filter = null;
	}

	V7Collection(V7Collection base, BSONBackedObject filter) {
		this.collection = base.collection;
		this.filter = merge(filter, base.filter);
	}

	private DBObject merge(BSONBackedObject filter, DBObject baseFilter) {
		DBObject x = filter == null ? new BasicDBObject() : filter
				.getDBObject();
		if (baseFilter != null) {
			x.putAll(baseFilter);
		}
		return x;
	}

	public DBCursor find(BSONBackedObject query) {
		DBObject q = merge(query, filter);
		return collection.find(q);
	}

	public DBCursor findIds(BSONBackedObject query) {
		DBObject q = merge(query, filter);
		return collection.find(q, nothing);
	}

	public DBObject findOne(Object id) {
		return collection.findOne(id);
	}

	public BSONBackedObject findOne(Object id, SchemaDefinition schema) {
		return BSONBackedObjectLoader.findOne(collection, id, schema);
	}

	public long count(BSONBackedObject query) {
		DBObject q = merge(query, filter);
		return collection.count(q);
	}
}
