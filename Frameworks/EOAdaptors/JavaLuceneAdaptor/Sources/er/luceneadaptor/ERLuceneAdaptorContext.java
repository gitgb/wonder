/* FSAdaptorContext - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package er.luceneadaptor;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOTemporaryGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;

/**
 * ERLuceneAdaptorContext provides the transaction support for the adaptor.
 * 
 * @author ak
 */
public class ERLuceneAdaptorContext extends EOAdaptorContext {

	IndexWriter _writer;

	public ERLuceneAdaptorContext(EOAdaptor adaptor) {
		super(adaptor);
	}

	@Override
	public NSDictionary _newPrimaryKey(EOEnterpriseObject object, EOEntity entity) {
		NSArray pkAttributes = entity.primaryKeyAttributes();
		if (pkAttributes.count() > 1) {
			throw new ERLuceneAdaptorException("Failed to generate primary key because " + entity.name() + " has a composite primary key.");
		}
		EOAttribute pkAttribute = (EOAttribute) pkAttributes.objectAtIndex(0);
		Object pkValue;
		String className = pkAttribute.className();
		String valueType = pkAttribute.valueType();
		if ("com.webobjects.foundation.NSData".equals(className)) {
			pkValue = new NSData(new EOTemporaryGlobalID()._rawBytes());
		} else {
			throw new IllegalArgumentException("Unknown value type '" + valueType + "' for '" + object + "' of entity '" + entity.name() + "'.");
		}
		NSDictionary pk = new NSDictionary<String, Object>(pkValue, pkAttribute.name());
		return pk;
	}

	public ERLuceneAdaptor adaptor() {
		return (ERLuceneAdaptor) super.adaptor();
	}

	@Override
	public void beginTransaction() {
		_writer = adaptor().createWriter();
		transactionDidBegin();
	}

	@Override
	public void commitTransaction() {
		try {
			_writer.commit();
			_writer.close();
			_writer = null;
			transactionDidCommit();
		} catch (CorruptIndexException e) {
			throw new ERLuceneAdaptorException("Commit failed: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new ERLuceneAdaptorException("Commit failed: " + e.getMessage(), e);
		}
	}

	@Override
	public EOAdaptorChannel createAdaptorChannel() {
		return new ERLuceneAdaptorChannel(this);
	}

	@Override
	public void handleDroppedConnection() {
		/* empty */
	}

	@Override
	public void rollbackTransaction() {
		try {
			_writer.rollback();
			_writer.close();
			_writer = null;
			transactionDidRollback();
		} catch (CorruptIndexException e) {
			throw new ERLuceneAdaptorException("Rollback failed: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new ERLuceneAdaptorException("Rollback failed: " + e.getMessage(), e);
		}
	}

	public IndexWriter writer() {
		return _writer;
	}
}