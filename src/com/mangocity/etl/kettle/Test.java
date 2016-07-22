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
	
//	 * @param path �ļ�Ŀ¼·��
//	 * @param name �ļ�����
//	 * @param type �ļ�����
//	 * @param parameters ��������(JSON��ʽ)
//	 * @param variables ����(JSON��ʽ)
//	 * @param arguments arg(JSON��ʽ)
//	 * @param exec_mode ִ��ģʽ: local/remote
//	 * @param remote_match Զ�̻���
//	 * @param pass_remote_server 1/0
//	 * @param log_level ��־����Ĭ��Basic
//	 * @param replay_date �ط����� yyyy/MM/dd hh:mm:ss
//	 * @param job_start job�����ڵ�
//	 * @param enable_safe_mode ���ð�ȫģʽ 1/0
//	 * @param clean_log_befor_exec ��ʼǰ�����־ 1/0
	
	public static void main(String[] args) throws Exception{
		KettleTaskExecutor kettleTaskExecutor = new KettleTaskExecutor();
		KettleTaskExecutorParamter.Builder builder = new KettleTaskExecutorParamter.Builder("/���˹���Ŀ¼/������", "Carte��������", "kjb");
		builder.execMode("remote");
		builder.passRemoteServer("1");
		builder.logLevel("Basic");
		KettleTaskExecutorParamter paramter = builder.build();
		kettleTaskExecutor.execute(paramter);
	}
}
