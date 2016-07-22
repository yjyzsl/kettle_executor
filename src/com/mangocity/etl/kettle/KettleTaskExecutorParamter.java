package com.mangocity.etl.kettle;

import java.util.Map;

public class KettleTaskExecutorParamter {

//	 * @param path 
//	 * @param name 
//	 * @param type 
//	 * @param parameters 
//	 * @param variables 
//	 * @param arguments 
//	 * @param exec_mode 
//	 * @param pass_remote_server 
//	 * @param log_level 
//	 * @param replay_date 
//	 * @param job_start 
//	 * @param enable_safe_mode 
//	 * @param clean_log_befor_exec 
	
	/**
	 * 文件目录路径
	 */
	private String taskPath;
	
	/**
	 * 文件名称
	 */
	private String taskName;
	
	/**
	 * 文件类型 (ktr, kjb)
	 */
	private String taskType;
	
	/**
	 * 命名参数(JSON格式)
	 */
	private Map<String, String> parameters;
	private String parametersStr;
	
	/**
	 * 变量(JSON格式)
	 */
	private Map<String, String> variables;
	private String variablesStr;
	
	/**
	 * arg(JSON格式)
	 */
	private Map<String, String> arguments;
	private String argumentsStr;
	
	/**
	 * 执行模式: local/remote
	 */
	private String execMode;
	
	/**
	 * 将导出的文件的发送到远程服务器
	 * 1/0
	 */
	private String passRemoteServer;
	
	/**
	 * 日志级别，默认Basic
	 */
	private String logLevel;
	
	/**
	 * job启动节点
	 */
	private String jobStart;
	
	/**
	 * 启用安全模式 1/0
	 */
	private String enableSafeMode;
	
	/**
	 * 开始前清空日志 1/0
	 */
	private String cleanLogBeforExec;
	
	/**
	 * 重放日期 yyyy/MM/dd hh:mm:ss
	 */
	private String replayDate;
	
	
	private String waitComplete;
	
	
	public String getWaitComplete() {
		return waitComplete;
	}
	public void setWaitComplete(String waitComplete) {
		this.waitComplete = waitComplete;
	}
	public String getReplayDate() {
		return replayDate;
	}
	public void setReplayDate(String replayDate) {
		this.replayDate = replayDate;
	}
	private KettleTaskExecutorParamter(Builder builder) {
		this.taskPath = builder.taskPath;
		this.taskName = builder.taskName;
		this.taskType = builder.taskType;
		this.parameters = builder.parameters;
		this.variables = builder.variables;
		this.arguments = builder.arguments;
		this.parametersStr = builder.parametersStr;
		this.variablesStr = builder.variablesStr;
		this.argumentsStr = builder.argumentsStr;
		this.execMode = builder.execMode;
		this.passRemoteServer = builder.passRemoteServer;
		this.logLevel = builder.logLevel;
		this.jobStart = builder.jobStart;
		this.enableSafeMode = builder.enableSafeMode;
		this.cleanLogBeforExec = builder.cleanLogBeforExec;
		this.replayDate = builder.replayDate;
		this.waitComplete = builder.waitComplete;
	}
	public static class Builder {
		/**
		 * 文件目录路径
		 */
		private String taskPath;
		
		/**
		 * 文件名称
		 */
		private String taskName;
		
		/**
		 * 文件类型 (ktr, kjb)
		 */
		private String taskType;
		
		/**
		 * 命名参数(JSON格式)
		 */
		private Map<String, String> parameters;
		private String parametersStr;
		
		/**
		 * 变量(JSON格式)
		 */
		private Map<String, String> variables;
		private String variablesStr;
		
		/**
		 * arg(JSON格式)
		 */
		private Map<String, String> arguments;
		private String argumentsStr;
		
		/**
		 * 执行模式: local/remote
		 */
		private String execMode;
		
		/**
		 * 1/0
		 */
		private String passRemoteServer;
		
		/**
		 * 日志级别，默认Basic
		 */
		private String logLevel;
		
		/**
		 * job启动节点
		 */
		private String jobStart;
		
		/**
		 * 启用安全模式 1/0
		 */
		private String enableSafeMode;
		
		/**
		 * 开始前清空日志 1/0
		 */
		private String cleanLogBeforExec;
		
		/**
		 * 重放日期 yyyy/MM/dd hh:mm:ss
		 */
		private String replayDate;
		
		private String waitComplete;
		
		public Builder(String taskPath, String taskName, String taskType) {
			this.taskPath = taskPath;
			this.taskName = taskName;
			this.taskType = taskType;
		}
		
		public Builder parameters(Map<String, String> val) {
			this.parameters = val;
			return this;
		}
		public Builder variables(Map<String, String> val) {
			this.variables = val;
			return this;
		}
		public Builder arguments(Map<String, String> val) {
			this.arguments = val;
			return this;
		}
		public Builder parametersStr(String val) {
			this.parametersStr = val;
			return this;
		}
		public Builder variablesStr(String val) {
			this.variablesStr = val;
			return this;
		}
		public Builder argumentsStr(String val) {
			this.argumentsStr = val;
			return this;
		}
		public Builder execMode(String val) {
			this.execMode = val;
			return this;
		}
		public Builder passRemoteServer(String val) {
			this.passRemoteServer = val;
			return this;
		}
		public Builder logLevel(String val) {
			this.logLevel = val;
			return this;
		}
		public Builder jobStart(String val) {
			this.jobStart = val;
			return this;
		}
		public Builder enableSafeMode(String val) {
			this.enableSafeMode = val;
			return this;
		}
		public Builder cleanLogBeforExec(String val) {
			this.cleanLogBeforExec = val;
			return this;
		}
		public Builder replayDate(String val) {
			this.replayDate = val;
			return this;
		}
		public Builder waitComplete(String val) {
			this.waitComplete = val;
			return this;
		}
		
		public KettleTaskExecutorParamter build() {
			return new KettleTaskExecutorParamter(this);
		}
	}
	
	public String getTaskPath() {
		return taskPath;
	}
	public void setTaskPath(String taskPath) {
		this.taskPath = taskPath;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public String getTaskType() {
		return taskType;
	}
	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}
	
	public Map<String, String> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	public String getParametersStr() {
		return parametersStr;
	}
	public void setParametersStr(String parametersStr) {
		this.parametersStr = parametersStr;
	}
	public Map<String, String> getVariables() {
		return variables;
	}
	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}
	public String getVariablesStr() {
		return variablesStr;
	}
	public void setVariablesStr(String variablesStr) {
		this.variablesStr = variablesStr;
	}
	public Map<String, String> getArguments() {
		return arguments;
	}
	public void setArguments(Map<String, String> arguments) {
		this.arguments = arguments;
	}
	public String getArgumentsStr() {
		return argumentsStr;
	}
	public void setArgumentsStr(String argumentsStr) {
		this.argumentsStr = argumentsStr;
	}
	public String getExecMode() {
		return execMode;
	}
	public void setExecMode(String execMode) {
		this.execMode = execMode;
	}
	public String getPassRemoteServer() {
		return passRemoteServer;
	}
	public void setPassRemoteServer(String passRemoteServer) {
		this.passRemoteServer = passRemoteServer;
	}
	public String getLogLevel() {
		return logLevel;
	}
	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}
	public String getJobStart() {
		return jobStart;
	}
	public void setJobStart(String jobStart) {
		this.jobStart = jobStart;
	}
	public String getEnableSafeMode() {
		return enableSafeMode;
	}
	public void setEnableSafeMode(String enableSafeMode) {
		this.enableSafeMode = enableSafeMode;
	}
	public String getCleanLogBeforExec() {
		return cleanLogBeforExec;
	}
	public void setCleanLogBeforExec(String cleanLogBeforExec) {
		this.cleanLogBeforExec = cleanLogBeforExec;
	}
	
	
}
