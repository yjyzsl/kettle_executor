package com.mangocity.etl.kettle;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.encryption.Encr;

public class Test {

	public static void mains(String[] args) throws Exception{
		KettleEnvironment.init();
		System.out.println(Encr.decryptPassword("Encrypted 2be98afc86aa7f2e4cb79ce10cc9da0ce"));;
		System.out.println(Encr.encryptPasswordIfNotUsingVariables("admin"));
		System.out.println(Encr.encryptPasswordIfNotUsingVariables("cluster"));
	}
	
//	 * @param path 文件目录路径
//	 * @param name 文件名称
//	 * @param type 文件类型
//	 * @param parameters 命名参数(JSON格式)
//	 * @param variables 变量(JSON格式)
//	 * @param arguments arg(JSON格式)
//	 * @param exec_mode 执行模式: local/remote
//	 * @param remote_match 远程机器
//	 * @param pass_remote_server 1/0
//	 * @param log_level 日志级别，默认Basic
//	 * @param replay_date 重放日期 yyyy/MM/dd hh:mm:ss
//	 * @param job_start job启动节点
//	 * @param enable_safe_mode 启用安全模式 1/0
//	 * @param clean_log_befor_exec 开始前清空日志 1/0
	
	public static void main(String[] args) throws Exception{
		KettleTaskExecutor kettleTaskExecutor = new KettleTaskExecutor();
		KettleTaskExecutorParamter.Builder builder = new KettleTaskExecutorParamter.Builder("/个人工作目录/梁鄂湘", "Carte环境测试", "kjb");
		builder.execMode("remote");
		builder.passRemoteServer("1");
		builder.logLevel("Basic");
		KettleTaskExecutorParamter paramter = builder.build();
		kettleTaskExecutor.execute(paramter);
	}
}
