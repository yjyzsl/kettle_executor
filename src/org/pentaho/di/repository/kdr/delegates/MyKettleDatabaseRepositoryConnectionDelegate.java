package org.pentaho.di.repository.kdr.delegates;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryConnectionDelegate;

public class MyKettleDatabaseRepositoryConnectionDelegate extends KettleDatabaseRepositoryConnectionDelegate{

	protected List<Object[]>           jobEntryAttributesBuffer;
	protected RowMetaInterface         jobEntryAttributesRowMeta;
	
	private KettleDatabaseRepositoryConnectionDelegate baseconnectionDelegate;
	
	public MyKettleDatabaseRepositoryConnectionDelegate(
			KettleDatabaseRepository repository, DatabaseMeta databaseMeta) {
		super(repository, databaseMeta);
	}
	
	 public MyKettleDatabaseRepositoryConnectionDelegate(
			KettleDatabaseRepository repository, DatabaseMeta databaseMeta,
			KettleDatabaseRepositoryConnectionDelegate baseconnectionDelegate) {
		super(repository, databaseMeta);
		this.baseconnectionDelegate = baseconnectionDelegate;
	}




	private class JobEntryAttributeComparator implements Comparator<Object[]> {

	    	public int compare(Object[] r1, Object[] r2) 
	    	{
	    		try {
	    			return jobEntryAttributesRowMeta.compare(r1, r2, KEY_POSITIONS);
	    		} catch (KettleValueException e) {
	    			return 0; // conversion errors
	    		}
	    	}
	    }
	 
	public synchronized void fillJobEntryAttributesBuffer(ObjectId id_job) throws KettleException
	{
	    String sql = "SELECT "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_ID_JOB)+", "
	    				+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY)+", "
	    				+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_NR)+", "
	    				+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_CODE)+", "
	    				+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_VALUE_NUM)+", "
	    				+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_VALUE_STR)+" "+
	                 "FROM "+databaseMeta.getQuotedSchemaTableCombination(null, KettleDatabaseRepository.TABLE_R_JOBENTRY_ATTRIBUTE) +" "+
	                 "WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_ID_JOB)+" = "+id_job+" "+
	                 "ORDER BY "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY)+", "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_CODE)+", "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_NR)
	                 ;
	    
	    jobEntryAttributesBuffer = baseconnectionDelegate.database.getRows(sql, -1);
	    jobEntryAttributesRowMeta = baseconnectionDelegate.database.getReturnRowMeta();
        
	    // must use java-based sort to ensure compatibility with binary search
	    // database ordering may or may not be case-insensitive
	    //
	    
        Collections.sort(jobEntryAttributesBuffer, new JobEntryAttributeComparator());  // in case db sort does not match our sort
	}

	public List<Object[]> getJobEntryAttributesBuffer() {
		return jobEntryAttributesBuffer;
	}

	public RowMetaInterface getJobEntryAttributesRowMeta() {
		return jobEntryAttributesRowMeta;
	}
	
	
	
}
