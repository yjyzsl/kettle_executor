package com.mangocity.etl.kettle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.www.SlaveServerJobStatus;
import org.pentaho.di.www.SlaveServerTransStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import com.google.gson.Gson;

public class KettleTaskExecutor {
	String APP_NAME = "MANGODATA_ETL_SCHEDULE";
	private static Class<?> PKG = KettleTaskExecutor.class;
	
	private Logger logger = LoggerFactory.getLogger(KettleTaskExecutor.class);
	private LogChannelInterface log = new LogChannel(APP_NAME);
	
	
	private String kettleConfHome;

	public SlaveServer getSlaveServer() {
		return slaveServer;
	}


	public void setSlaveServer(SlaveServer slaveServer) {
		this.slaveServer = slaveServer;
	}

	private String repoName; //资源库名称
	private String repoUserName; //资源库用户名
	private String repoPassword; //资源库密码
	private String slaveServerHost;
	private String slaveServerUserName;
	private String slaveServerPassword;
	private String slaveServerPort;
	private String slaveServerName;
	
	private Properties configProperties;
	private SlaveServer slaveServer;
	private Repository repository;
	
	public KettleTaskExecutor() {
		logger.info("初始化...");
		init();
	}

	
	private void loadConfig() throws Exception {
		FileSystemResource resource = null;
		try {
			String userHome = System.getProperty("user.home");
			kettleConfHome = userHome+"/.kettle";
			String slaveServerConfFilePath = kettleConfHome+"/kettle_executor.properties";
			configProperties = new Properties();
			resource = new FileSystemResource(slaveServerConfFilePath);
			configProperties.load(resource.getInputStream());
			this.repoName = StringUtils.trim(configProperties.getProperty("repository.name"));
			this.repoUserName = StringUtils.trim(configProperties.getProperty("repository.username"));
			this.repoPassword = StringUtils.trim(configProperties.getProperty("repository.password"));
			
			this.slaveServerName = StringUtils.trim(configProperties.getProperty("kettle.slave.name"));
			this.slaveServerHost = StringUtils.trim(configProperties.getProperty("kettle.slave.host"));
			this.slaveServerPort = StringUtils.trim(configProperties.getProperty("kettle.slave.prot"));
			this.slaveServerUserName = StringUtils.trim(configProperties.getProperty("kettle.slave.username"));
			this.slaveServerPassword = StringUtils.trim(configProperties.getProperty("kettle.slave.password"));
			if (StringUtils.startsWith(slaveServerPassword, "Encrypted")) {
				this.slaveServerPassword = Encr.decryptPassword(slaveServerPassword);
			}
			if (StringUtils.startsWith(repoPassword, "Encrypted")) {
				this.repoPassword = Encr.decryptPassword(repoPassword);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != resource) {
				resource.getInputStream().close();
			}
		}
	}
	
	public void init() {
		try {
			KettleEnvironment.init();
			loadConfig();
			this.repository = buildRepository();
			this.slaveServer = new SlaveServer(slaveServerName, slaveServerHost, slaveServerPort, slaveServerUserName, slaveServerPassword);
			logger.info("成功连接到SlaveServer->{}, {}, {}", slaveServerName, slaveServerHost+":"+slaveServerPort, slaveServerUserName);
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	
	public Repository buildRepository() throws Exception{
		RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
		repositoriesMeta.readData();
		RepositoryMeta repositoryMeta = repositoriesMeta.findRepository(repoName);
		PluginRegistry pluginRegistry = PluginRegistry.getInstance();
		Repository repository = pluginRegistry.loadClass(RepositoryPluginType.class, repositoryMeta, Repository.class);
		repository.init(repositoryMeta);
		repository.connect(repoUserName, repoPassword);
		logger.info("成功连接到kettle资料库->{},{}", repoName, repoUserName);
		return repository;
	}
	
	private RepositoryDirectoryInterface getDirectory(String path) throws Exception {
		RepositoryDirectoryInterface repositoryDirectoryInterface = repository.loadRepositoryDirectoryTree();
		if ((path != null) && (!"/".equals(path)) && (!"".equals(path))) {
			repositoryDirectoryInterface = repositoryDirectoryInterface.findDirectory(path);
		}
		return repositoryDirectoryInterface;
	}
	
	
	
	public boolean execute(KettleTaskExecutorParamter executorParamter) throws Exception {
		try {
			RepositoryDirectoryInterface repdir = getDirectory(executorParamter.getTaskPath());
			if (null != repdir) {
				if ("ktr".equals(executorParamter.getTaskType())) {
					TransMeta transMeta = repository.loadTransformation(executorParamter.getTaskName(), repdir, null, true, null);
					TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();
					if (StringUtils.equalsIgnoreCase(executorParamter.getExecMode(), "remote")) {
						transExecutionConfiguration.setExecutingClustered(false);
						transExecutionConfiguration.setExecutingLocally(false);
						transExecutionConfiguration.setExecutingRemotely(true);
//						transExecutionConfiguration.setRemoteServer(getSlaveServerInstance(transMeta.getSlaveServers(), remote_match));
						transExecutionConfiguration.setRemoteServer(slaveServer);
						transExecutionConfiguration.setPassingExport(StringUtils.equals(executorParamter.getPassRemoteServer(), "1"));
					} else {
						transExecutionConfiguration.setExecutingClustered(false);
						transExecutionConfiguration.setExecutingRemotely(false);
						transExecutionConfiguration.setExecutingLocally(true);
						
						transExecutionConfiguration.setPassingExport(false);
					}
					if (null != executorParamter.getParameters() && executorParamter.getParameters().size() > 0) {
						transExecutionConfiguration.setParams(executorParamter.getParameters());
					} else {
						if (StringUtils.isNotBlank(executorParamter.getParametersStr())) {
							transExecutionConfiguration.setParams(resoveEnvForJson(executorParamter.getParametersStr()));
						}
					}
					
					if (null != executorParamter.getVariables() && executorParamter.getVariables().size() > 0) {
						transExecutionConfiguration.setVariables(executorParamter.getVariables());
					} else {
						if (StringUtils.isNotBlank(executorParamter.getVariablesStr())) {
							transExecutionConfiguration.setVariables(resoveEnvForJson(executorParamter.getVariablesStr()));
						}
					}
					
					if (null != executorParamter.getArguments() && executorParamter.getArguments().size() > 0) {
						transExecutionConfiguration.setArguments(executorParamter.getArguments());
					} else {
						if (StringUtils.isNotBlank(executorParamter.getArgumentsStr())) {
							transExecutionConfiguration.setArguments(resoveEnvForJson(executorParamter.getArgumentsStr()));
						}
					}
					
					if (StringUtils.isNotBlank(executorParamter.getReplayDate())) {
						transExecutionConfiguration.setReplayDate(DateUtils.getDateByStr(executorParamter.getReplayDate(), new SimpleDateFormat("yyyy/MM/dd hh:mm:ss")));
					}
					if (StringUtils.isNotBlank(executorParamter.getEnableSafeMode())) {
						transExecutionConfiguration.setSafeModeEnabled(StringUtils.equals(executorParamter.getEnableSafeMode(), "1"));
					}
					if (StringUtils.isNotBlank(executorParamter.getLogLevel())) {
						transExecutionConfiguration.setLogLevel(LogLevel.getLogLevelForCode(executorParamter.getLogLevel()));
					} else {
						transExecutionConfiguration.setLogLevel(LogLevel.BASIC);
					}
					transExecutionConfiguration.setClearingLog(StringUtils.equals(executorParamter.getCleanLogBeforExec(), "1"));
					transExecutionConfiguration.setRepository(repository);
					
					if (transExecutionConfiguration.isExecutingRemotely()) {
						executeTransForRemote(transMeta, transExecutionConfiguration, executorParamter);
					} else {
						executeTransForLocal(transMeta, transExecutionConfiguration, executorParamter);
					}
					
				} else if ("kjb".equals(executorParamter.getTaskType())) {
					JobMeta jobMeta = repository.loadJob(executorParamter.getTaskName(), repdir, null, null);
					JobExecutionConfiguration jobExecutionConfiguration = new JobExecutionConfiguration();
					jobExecutionConfiguration.setExpandingRemoteJob(true);
					if (StringUtils.equalsIgnoreCase(executorParamter.getExecMode(), "remote")) {
						jobExecutionConfiguration.setExecutingLocally(false);
						jobExecutionConfiguration.setExecutingRemotely(true);
						jobExecutionConfiguration.setRemoteServer(slaveServer);
//						jobExecutionConfiguration.setRemoteServer(getSlaveServerInstance(jobMeta.getSlaveServers(), remote_match));
						jobExecutionConfiguration.setPassingExport(StringUtils.equals(executorParamter.getPassRemoteServer(), "1"));
					} else {
						jobExecutionConfiguration.setExecutingRemotely(false);
						jobExecutionConfiguration.setExecutingLocally(true);
						
						jobExecutionConfiguration.setPassingExport(false);
					}
					if (null != executorParamter.getParameters() && executorParamter.getParameters().size() > 0) {
						jobExecutionConfiguration.setParams(executorParamter.getParameters());
					} else {
						if (StringUtils.isNotBlank(executorParamter.getParametersStr())) {
							jobExecutionConfiguration.setParams(resoveEnvForJson(executorParamter.getParametersStr()));
						}
					}
					
					if (null != executorParamter.getVariables() && executorParamter.getVariables().size() > 0) {
						jobExecutionConfiguration.setVariables(executorParamter.getVariables());
					} else {
						if (StringUtils.isNotBlank(executorParamter.getVariablesStr())) {
							jobExecutionConfiguration.setVariables(resoveEnvForJson(executorParamter.getVariablesStr()));
						}
					}
					
					if (null != executorParamter.getArguments() && executorParamter.getArguments().size() > 0) {
						jobExecutionConfiguration.setArguments(executorParamter.getArguments());
					} else {
						if (StringUtils.isNotBlank(executorParamter.getArgumentsStr())) {
							jobExecutionConfiguration.setArguments(resoveEnvForJson(executorParamter.getArgumentsStr()));
						}
					}
					System.out.println(jobExecutionConfiguration.getArguments().toString());
					
					if (StringUtils.isNotBlank(executorParamter.getReplayDate())) {
						jobExecutionConfiguration.setReplayDate(DateUtils.getDateByStr(executorParamter.getReplayDate(), new SimpleDateFormat("yyyy/MM/dd hh:mm:ss")));
					}
					if (StringUtils.isNotBlank(executorParamter.getEnableSafeMode())) {
						jobExecutionConfiguration.setSafeModeEnabled(StringUtils.equals(executorParamter.getEnableSafeMode(), "1"));
					}
					if (StringUtils.isNotBlank(executorParamter.getLogLevel())) {
						jobExecutionConfiguration.setLogLevel(LogLevel.getLogLevelForCode(executorParamter.getLogLevel()));
					} else {
						jobExecutionConfiguration.setLogLevel(LogLevel.BASIC);
					}
					
					jobExecutionConfiguration.setRepository(repository);
					if (StringUtils.isNotBlank(executorParamter.getJobStart())) {
						JobEntryCopy startJobEntryCopy = getJobEntryCopyForName(jobMeta, executorParamter.getJobStart());
						if (null != startJobEntryCopy) {
							jobExecutionConfiguration.setStartCopyName(startJobEntryCopy.getName());
							jobExecutionConfiguration.setStartCopyNr(startJobEntryCopy.getNr());
						}
					}
					
					if (jobExecutionConfiguration.isExecutingRemotely()) {
						executeJobForRemote(jobMeta, jobExecutionConfiguration, executorParamter);
					} else {
						executeJobForLocal(jobMeta, jobExecutionConfiguration, executorParamter);
					}
				}
			} else {
				System.out.println("目录未找到");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != repository && repository.isConnected()) {
				repository.disconnect();
			}
		}
		return false;
	}
	
	private boolean executeTransForRemote(TransMeta transMeta, TransExecutionConfiguration executionConfiguration, KettleTaskExecutorParamter executorParamter) {
		boolean flag = false;
		try {
			log.logMinimal("ETL--TRANS Start of run");
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
			Calendar calendar = Calendar.getInstance();
			Date startDate = calendar.getTime();
			
			transMeta.activateParameters();
			if (null != executionConfiguration.getRemoteServer()) {
				String carteObjectId = Trans.sendToSlaveServer(transMeta, executionConfiguration, executionConfiguration.getRepository(), repository.getMetaStore());
				log.logMinimal("ETL--TRANS Finished, Return Value="+carteObjectId);
				calendar = Calendar.getInstance();
				String startDateStr = simpleDateFormat.format(startDate).toString();
				
				if (StringUtils.isNotBlank(executorParamter.getWaitComplete()) && StringUtils.equals(executorParamter.getWaitComplete(), "1")) {
					waitForTransExecute(transMeta, executionConfiguration, carteObjectId);
				}
				
				Date endDate = calendar.getTime();
				String endDateStr = simpleDateFormat.format(endDate).toString();
				log.logMinimal("ETL--TRANS Start=" + startDateStr + ", Stop="+ endDateStr);
				long interval = endDate.getTime() - startDate.getTime();
				log.logMinimal("ETL--TRANS Processing ended after " + interval/ 1000L + " seconds.");
				
				flag = true;
			} else {
				
				throw new RuntimeException("远程服务器不存在");
			}
		} catch (KettleException e) {
			e.printStackTrace();
		}
		return flag;
	}
	
	private boolean executeTransForLocal(TransMeta transMeta, TransExecutionConfiguration executionConfiguration, KettleTaskExecutorParamter executorParamter) {
		boolean flag = false;
		
		Trans trans = null;
		try {
			// Set the requested logging level..
			//
			DefaultLogLevel.setLogLevel(executionConfiguration.getLogLevel());

			transMeta.injectVariables(executionConfiguration.getVariables());

			// Set the named parameters
			Map<String, String> paramMap = executionConfiguration.getParams();
			Set<String> keys = paramMap.keySet();
			for (String key : keys) {
				transMeta.setParameterValue(key, Const.NVL(paramMap.get(key), "")); //$NON-NLS-1$
			}

			transMeta.activateParameters();


			// Also make sure to clear the log entries in the central log store
			// & registry
			//

			// Important: even though transMeta is passed to the Trans
			// constructor, it is not the same object as is in memory
			// To be able to completely test this, we need to run it as we would
			// normally do in pan
			//
			trans = new Trans(transMeta, 
					transMeta.getRepository(),
					transMeta.getName(), 
					transMeta.getRepositoryDirectory().getPath(), 
					transMeta.getFilename());
			trans.setLogLevel(executionConfiguration.getLogLevel());
			trans.setReplayDate(executionConfiguration.getReplayDate());
			trans.setRepository(executionConfiguration.getRepository());
			trans.setMonitored(true);
			log.logBasic(BaseMessages.getString(PKG, "TransLog.Log.TransformationOpened")); //$NON-NLS-1$
		} catch (KettleException e) {
			trans = null;
			log.logError(
					BaseMessages.getString(PKG, "TransLog.Dialog.ErrorOpeningTransformation.Title")
							+ "-----" + BaseMessages.getString(PKG, "TransLog.Dialog.ErrorOpeningTransformation.Message"),
					e);
		}
		
		try {
			if (trans != null) {
				Map<String, String> arguments = executionConfiguration.getArguments();
				final String args[];
				if (arguments != null)
					args = convertArguments(arguments);
				else
					args = null;

				log.logMinimal(BaseMessages.getString(PKG, "TransLog.Log.LaunchingTransformation") + trans.getTransMeta().getName() + "]..."); //$NON-NLS-1$ //$NON-NLS-2$
				trans.setSafeModeEnabled(executionConfiguration.isSafeModeEnabled());

				// Launch the step preparation in a different thread.
				// That way Spoon doesn't block anymore and that way we can follow
				// the progress of the initialization
				//
				log.logMinimal("ETL--TRANS Start of run");
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
				Calendar calendar = Calendar.getInstance();
				Date startDate = calendar.getTime();
				
				try {
					trans.prepareExecution(args);
				} catch (KettleException e) {
					log.logError(trans.getName()+ ": preparing transformation execution failed", e); //$NON-NLS-1$
					throw new RuntimeException("预处理转换失败", e);
				}
				if (trans.isReadyToStart()) {
					trans.startThreads();
					trans.waitUntilFinished();
					log.logMinimal("ETL--TRANS Finished");
					
					flag = true;
				} else {
					log.logMinimal("ETL-TRNAS UnReady.");
				}
				
				calendar = Calendar.getInstance();
				Date endDate = calendar.getTime();
				String startDateStr = simpleDateFormat.format(startDate).toString();
				String endDateStr = simpleDateFormat.format(endDate).toString();
				log.logMinimal("ETL--TRANS Start=" + startDateStr + ", Stop="+ endDateStr);
				long interval = endDate.getTime() - startDate.getTime();
				log.logMinimal("ETL--TRANS Processing ended after " + interval/ 1000L + " seconds.");
				
				log.logMinimal(BaseMessages.getString(PKG, "TransLog.Log.StartedExecutionOfTransformation")); //$NON-NLS-1$
			}
		} catch (Exception e) {
			flag = false;
			log.logError("ETL--JOB Start Error!!!["+e.getLocalizedMessage()+"]");
			e.printStackTrace();
		}
		return flag;
	}
	
	

	private String[] convertArguments(Map<String, String> arguments) {
		String[] argumentNames = arguments.keySet().toArray(new String[arguments.size()]);
		Arrays.sort(argumentNames);

		String args[] = new String[argumentNames.length];
		for (int i = 0; i < args.length; i++) {
			String argumentName = argumentNames[i];
			args[i] = arguments.get(argumentName);
		}
		return args;
	}
	
	public SlaveServer getSlaveServerInstance(List<SlaveServer> slaveServers, String slaveName) {
		if (null != slaveServers && slaveServers.size() > 0) {
			for (SlaveServer slaveServer : slaveServers) {
				if (slaveServer.getName().equals(slaveName)) {
					return slaveServer;
				}
			}
		}
		return null;
	}
	
	public JobEntryCopy getJobEntryCopyForName(JobMeta jobMeta, String name) {
		List<JobEntryCopy> list = null;
		if (null != jobMeta && null != (list = jobMeta.getJobCopies())) {
			for (JobEntryCopy jobEntryCopy : list) {
				if (StringUtils.equals(jobEntryCopy.getName(), name)) {
					return jobEntryCopy;
				}
			}
		}
		return null;
	}
	
	private Map<String, String> resoveEnvForJson(String jsonStr) {
		Gson gson = new Gson();
		List<Map<String, String>> params = new ArrayList<Map<String,String>>();
		params = gson.fromJson(jsonStr, params.getClass());
		return resoveEnv(params);
	}
	
	private Map<String, String> resoveEnv(List<Map<String, String>> envs) {
		Map<String, String> env = new HashMap<String, String>();
		if (null != env && env.size() > 0 ) {
			for (Map<String, String> map : envs) {
				String name = map.get("name");
				String value = map.get("value");
				env.put(name, value);
			}
		}
		return env;
	}
	
	private boolean executeJobForRemote(JobMeta jobMeta, JobExecutionConfiguration executionConfiguration, KettleTaskExecutorParamter executorParamter) {
		boolean flag = false;
		try {
			log.logMinimal("ETL--JOB Start of run");
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
			Calendar calendar = Calendar.getInstance();
			Date startDate = calendar.getTime();
			
			jobMeta.activateParameters();
			if (null != executionConfiguration.getRemoteServer()) {
				String startDateStr = simpleDateFormat.format(startDate).toString();
				String carteObjectId = Job.sendToSlaveServer(jobMeta, executionConfiguration, executionConfiguration.getRepository(), repository.getMetaStore());
				log.logMinimal("ETL--JOB Finished! Return Value="+carteObjectId);
				if (StringUtils.isNotBlank(executorParamter.getWaitComplete()) && StringUtils.equals(executorParamter.getWaitComplete(), "1")) {
					waitForJobExecute(jobMeta, executionConfiguration, carteObjectId);
				}
				
				calendar = Calendar.getInstance();
				Date endDate = calendar.getTime();
				String endDateStr = simpleDateFormat.format(endDate).toString();
				log.logMinimal("ETL--JOB Start=" + startDateStr + ", Stop=" + endDateStr);
				long interval = endDate.getTime() - startDate.getTime();
				log.logMinimal("ETL--JOB Processing ended after " + interval / 1000L + " seconds.");
				
				flag = true;
			} else {
				
				throw new RuntimeException("远程服务器不存在");
			}
		} catch (KettleException e) {
			e.printStackTrace();
		}
		return flag;
	}
	
	private void waitForJobExecute(JobMeta jobMeta, JobExecutionConfiguration executionConfiguration, String carteObjectId) {
		try {
			SlaveServer jobRemoteServer = executionConfiguration.getRemoteServer();
			Thread.sleep(1000);
			while (true) {
				SlaveServerJobStatus jobStatus = jobRemoteServer.getJobStatus(jobMeta.getName(), carteObjectId, 0);
				String statusDescription = jobStatus.getStatusDescription();
				if (StringUtils.equalsIgnoreCase(statusDescription, "Running")) {
					Thread.sleep(1000);
				} else {
					log.logMinimal("ETL--JOB-"+carteObjectId+" "+jobMeta.getName()+" 执行完成");
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void waitForTransExecute(TransMeta transMeta, TransExecutionConfiguration executionConfiguration, String carteObjectId) {
		try {
			SlaveServer jobRemoteServer = executionConfiguration.getRemoteServer();
			Thread.sleep(1000);
			while (true) {
				SlaveServerTransStatus transStatus = jobRemoteServer.getTransStatus(transMeta.getName(), carteObjectId, 0);
				String statusDescription = transStatus.getStatusDescription();
				if (StringUtils.equalsIgnoreCase(statusDescription, "Running")) {
					Thread.sleep(1000);
				} else {
					log.logMinimal("ETL--TRANS-"+carteObjectId+" "+transMeta.getName()+" 执行完成");
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean executeJobForLocal(JobMeta jobMeta, JobExecutionConfiguration executionConfiguration, KettleTaskExecutorParamter executorParamter) {
		boolean flag = false;
		try {
			Job job = new Job(jobMeta.getRepository(), jobMeta);
			job.setLogLevel(executionConfiguration.getLogLevel());
			// job = new Job(jobMeta.getName(), jobMeta.getFilename(), null);
			// job.open(spoon.rep, jobMeta.getFilename(), jobMeta.getName(),
			// jobMeta.getRepositoryDirectory().getPath(), spoon);

			job.getJobMeta().setArguments(jobMeta.getArguments());
			job.shareVariablesWith(jobMeta);
			job.setInteractive(true);

			// If there is an alternative start job entry, pass it to the job
			//
			if (!Const.isEmpty(executionConfiguration.getStartCopyName())) {
				JobEntryCopy startJobEntryCopy = jobMeta.findJobEntry(
						executionConfiguration.getStartCopyName(),
						executionConfiguration.getStartCopyNr(), false);
				job.setStartJobEntryCopy(startJobEntryCopy);
			}

			// Set the named parameters
			Map<String, String> paramMap = executionConfiguration.getParams();
			Set<String> keys = paramMap.keySet();
			for (String key : keys) {
				job.getJobMeta().setParameterValue(key, Const.NVL(paramMap.get(key), ""));
			}
			
			log.logMinimal("ETL--JOB Start of run");
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
			Calendar calendar = Calendar.getInstance();
			Date startDate = calendar.getTime();
			
			job.getJobMeta().activateParameters();
			job.beginProcessing();
			log.logMinimal("正在开始任务..."); //$NON-NLS-1$
			job.start();
			JobTracker jobTracker = job.getJobTracker();
			job.join();
			log.logMinimal("ETL--JOB Finished!");
			calendar = Calendar.getInstance();
			Date endDate = calendar.getTime();
			String startDateStr = simpleDateFormat.format(startDate).toString();
			String endDateStr = simpleDateFormat.format(endDate).toString();
			log.logMinimal("ETL--JOB Start=" + startDateStr + ", Stop=" + endDateStr);
			long interval = endDate.getTime() - startDate.getTime();
			log.logMinimal("ETL--JOB Processing ended after " + interval / 1000L + " seconds.");
			
			////////////////////////////////
			flag = true;
			
		} catch (Exception e) {
			log.logError("不能打开任务:任务打开失败", e);
		}
		return flag;
		
	}
	
	
}
