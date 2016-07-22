package com.mangocity.etl.kettle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class TaskRunner {

	private static void useage() {
		String userHome = System.getProperty("user.home");
		System.err.println(userHome+"/kettle/task_exec/task_runner.sh 配置文件");
	}
	static Set<String> types = new LinkedHashSet<String>(Arrays.asList("kjb", "ktr"));
	static Set<String> execModes = new LinkedHashSet<String>(Arrays.asList("remote", "local"));
	static Set<String> logLevels = new LinkedHashSet<String>(Arrays.asList("Nothing", "Error", "Minimal", "Basic", "Detailed", "Debug", "Rowlevel"));
	
	private static KettleTaskExecutorParamter resoveConfig(String[] args) {
		if (null == args || args.length <= 0) {
			useage();
			System.exit(0);
		}
		String inputPath = args[0];
		if (StringUtils.isBlank(inputPath)) {
			useage();
			System.exit(0);
		}
		try {
			List<String> lines = FileUtils.readLines(new File(inputPath), "utf-8");
			Map<String, String> parameters = new LinkedHashMap<String, String>();
			Map<String, String> nameParameters = new LinkedHashMap<String, String>();
			Map<String, String> variables = new LinkedHashMap<String, String>();
			List<String> arguments = new ArrayList<String>();
			for (String line : lines) {
				String str = null;
				if (StringUtils.isNotBlank(line)) {
					str = line;
					str = StringUtils.trim(str);
				}
				
				if (StringUtils.isBlank(str)) {
					continue;
				}
				if (StringUtils.startsWith(str, "#")) {
					continue;
				}
				if (StringUtils.contains(str, "=")) {
					String name = StringUtils.substringBefore(str, "=");
					String value = StringUtils.substringAfter(str, "=");
					if (StringUtils.startsWith(name, "parameters.")) {
						String nameParamName = StringUtils.substringAfter(name, "parameters.");
						nameParameters.put(nameParamName, value);
					} else if (StringUtils.startsWith(name, "variables.")) {
						String variableName = StringUtils.substringAfter(name, "variables.");
						variables.put(variableName, value);
					} else if (StringUtils.equals(name, "arguments")) {
						arguments.add(value);
					} else {
						parameters.put(name, value);
					}
				}
			}
			String taskPath = parameters.get("path");
			if (StringUtils.isBlank(taskPath)) {
				System.err.println("JOB/Trans路径不能为空");
				System.exit(-1);
			}
			String taskName = parameters.get("name");
			if (StringUtils.isBlank(taskPath)) {
				System.err.println("JOB/Trans名称不能为空");
				System.exit(-1);
			}
			String taskType = parameters.get("type");
			if (StringUtils.isBlank(taskPath)) {
				System.err.println("类型名称不能为空");
				System.exit(-1);
			}
			if (!types.contains(taskType)) {
				System.err.println("任务类型必须是kjb或者ktr");
				System.exit(-1);
			}
			KettleTaskExecutorParamter.Builder builder = new KettleTaskExecutorParamter.Builder(taskPath, taskName, taskType);
			if (parameters.containsKey("exec_mode")) {
				if (execModes.contains(parameters.get("exec_mode"))) {
					builder.execMode(parameters.get("exec_mode"));
				} else {
					System.err.println("执行模式（exec_mode）必须是："+execModes.toString());
					System.exit(-1);
				}
			} else {
				builder.execMode("remote");
			}
			
			if (parameters.containsKey("pass_remote_server")) {
				builder.passRemoteServer(parameters.get("pass_remote_server"));
			}
			if (parameters.containsKey("log_level")) {
				if (logLevels.contains(parameters.get("log_level"))) {
					builder.logLevel(parameters.get("log_level"));
				} else {
					System.err.println("日志级别（log_level）必须是："+logLevels.toString());
					System.exit(-1);
				}
			} else {
				builder.logLevel("Basic");
			}
			
			if (parameters.containsKey("enable_safe_mode")) {
				builder.enableSafeMode(parameters.get("enable_safe_mode"));
			}
			if (parameters.containsKey("clean_log_befor_exec")) {
				builder.cleanLogBeforExec(parameters.get("clean_log_befor_exec"));
			}
			if (parameters.containsKey("replay_date")) {
				builder.replayDate(parameters.get("replay_date"));
			}
			if (parameters.containsKey("wait_complete")) {
				builder.waitComplete(parameters.get("wait_complete"));
			}
			if (nameParameters.size() > 0) {
				builder.parameters(nameParameters);
			}
			if (variables.size() > 0) {
				builder.variables(variables);
			}
			if (arguments.size() > 0) {
				Map<String, String> argumentMap = new LinkedHashMap<String, String>();
				int i=0; 
				for (String argument : arguments) {
					argumentMap.put("arg "+(i+1), argument);
					i++;
				}
				builder.arguments(argumentMap);
			}
			KettleTaskExecutorParamter kettleTaskExecutorParamter = builder.build();
			return kettleTaskExecutorParamter;
		} catch (IOException e) {
			System.err.println("文件读取错误->"+inputPath);
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
//		args = new String[]{"D:/workspaces/dsp_workspace/kettle_executor/kettle_job_test2.sh"};
		KettleTaskExecutorParamter kettleTaskExecutorParamter = resoveConfig(args);
		if (null == kettleTaskExecutorParamter) {
			System.err.println("配置解析错误");
			System.exit(-2);
		}
		try {
			KettleTaskExecutor kettleTaskExecutor = new KettleTaskExecutor();
			kettleTaskExecutor.execute(kettleTaskExecutorParamter);
		} catch (Exception e) {
			System.err.println("任务执行错误:"+e.getMessage());
			e.printStackTrace();
		}
	}
}
