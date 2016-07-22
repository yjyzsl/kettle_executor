package com.mangocity.etl.kettle;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.www.SlaveServerJobStatus;
import org.pentaho.di.www.SlaveServerStatus;

public class Test2 {

	public static void main(String[] args) throws Exception{
		KettleTaskExecutor kettleTaskExecutor = new KettleTaskExecutor();
		SlaveServer slaveServer = kettleTaskExecutor.getSlaveServer();
		SlaveServerStatus status = slaveServer.getStatus();
		List<SlaveServerJobStatus> jobStatusList = status.getJobStatusList();
		for (SlaveServerJobStatus slaveServerJobStatus : jobStatusList) {
			System.out.println(slaveServerJobStatus.getJobName());
			System.out.println(slaveServerJobStatus.getLoggingString());
			System.out.println(slaveServerJobStatus.getStatusDescription());
		}
		
	}
}
